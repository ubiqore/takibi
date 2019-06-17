package ubiqore.takibi.infrastructure.endpoint


import cats.data.Validated.Valid
import cats.data._
import cats.effect.{ContextShift, Effect}
import cats.implicits._
import io.circe.Json
import org.http4s.StaticFile
import org.http4s.headers.Location
import org.http4s.Uri._
import org.webjars.WebJarAssetLocator
//import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, QueryParamDecoder}

import scala.language.higherKinds
import ubiqore.takibi.domain.{ProjectAlreadyExistsError, ProjectNotFoundError}
import ubiqore.takibi.domain.projects.{Project, ProjectService, ProjectStatus}
import scala.concurrent.ExecutionContext.global

class ProjectEndpoints[F[_]: Effect:ContextShift] extends Http4sDsl[F] {

  import Pagination._

  /* Parses out status query param which could be multi param */
  implicit val statusQueryParamDecoder: QueryParamDecoder[ProjectStatus] =
    QueryParamDecoder[String].map(ProjectStatus.withName)

  /* Relies on the statusQueryParamDecoder implicit, will parse out a possible multi-value query parameter */
  object StatusMatcher extends OptionalMultiQueryParamDecoderMatcher[ProjectStatus]("status")

  /* Parses out tag query param, which could be multi-value */
  object BaseMatcher extends OptionalMultiQueryParamDecoderMatcher[String]("bases")

  /* Need Instant Json Encoding */
  import io.circe.java8.time._

  implicit val projectDecoder: EntityDecoder[F, Project] = jsonOf[F, Project]


  private def yamlSwaggerEndpoint () :HttpRoutes[F] =

    HttpRoutes.of[F] {
      case req @ GET -> Root / "swagger.yaml" =>
      StaticFile.fromResource("/swagger.yaml",global, Some(req)).getOrElseF(NotFound())

      case _ @ GET -> `swaggerUiPath` / "config.json" =>
        //Specifies Swagger spec URL
        Ok(Json.obj("url" -> Json.fromString(s"$applicationUrl/swagger.yaml")))
      //Entry point to Swagger UI
      case _ @ GET -> `swaggerUiPath` =>
        PermanentRedirect(Location(uri("swagger-ui/index.html")))
      case req @ GET -> path if path.startsWith(swaggerUiPath) =>
        //Serves Swagger UI files
        val file = "/" + path.toList.drop(swaggerUiPath.toList.size).mkString("/")
        (if(file == "/index.html") {
          StaticFile.fromResource("/swagger-ui/index.html", global,Some(req))
        } else {
          StaticFile.fromResource(swaggerUiResources + file, global, Some(req))
        }).getOrElseF(NotFound())

    }

  private val applicationUrl = "http://localhost:7676" //should be configurable to match your deploy
  private val swaggerUiPath = Path("swagger-ui")
  private val swaggerUiResources = s"/META-INF/resources/webjars/swagger-ui/$swaggerUiVersion"

  private lazy val swaggerUiVersion: String = {
    Option(new WebJarAssetLocator().getWebJars.get("swagger-ui")).fold {
      throw new RuntimeException(s"Could not detect swagger-ui webjar version")
    } { version =>
      version
    }
  }

  /*



val service = org.http4s.HttpService[IO] {
  case request @ GET -> `swaggerUiPath` / "config.json" =>
    //Specifies Swagger spec URL
    Ok(Json.obj("url" -> Json.fromString(s"$applicationUrl/swagger.yaml")))
    //Entry point to Swagger UI
  case request @ GET -> `swaggerUiPath` =>
    PermanentRedirect(Location(uri("swagger-ui/index.html")))
  case request @ GET -> path if path.startsWith(swaggerUiPath) =>
    //Serves Swagger UI files
    val file = "/" + path.toList.drop(swaggerUiPath.toList.size).mkString("/")
    (if(file == "/index.html") {
      StaticFile.fromResource("/swagger-ui/index.html", Some(request))
    } else {
      StaticFile.fromResource(swaggerUiResources + file, Some(request))
    }).getOrElseF(NotFound())
}

private val swaggerUiResources = s"/META-INF/resources/webjars/swagger-ui/$swaggerUiVersion"

private lazy val swaggerUiVersion: String = {
  Option(new WebJarAssetLocator().getWebJars.get("swagger-ui")).fold {
    throw new RuntimeException(s"Could not detect swagger-ui webjar version")
  } { version =>
    version
  }
}
  */

  private def createProjectEndpoint(projectService: ProjectService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "projects" =>
        val action = for {
          project <- req.as[Project]
          result <- projectService.create(project).value
        } yield result

        action.flatMap {
          case Right(saved) =>
            Ok(saved.asJson)
          case Left(ProjectAlreadyExistsError(existing)) =>
            Conflict(s"The project ${existing.project_name} of base iri ${existing.project_base_iri} already exists")
        }
    }

  private def updateProjectEndpoint(projectService: ProjectService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ PUT -> Root / "projects" / LongVar(projectId) =>
        val action: F[Either[ProjectNotFoundError.type, Project]] = for {
          project <- req.as[Project]
          updated = project.copy(project_id = Some(projectId))
          result <- projectService.update(updated).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(ProjectNotFoundError) => NotFound("The project was not found")
        }
    }

  private def getProjectEndpoint(projectService: ProjectService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "projects" / LongVar(id) =>
        projectService.get(id).value.flatMap {
          case Right(found) => Ok(found.asJson)
          case Left(ProjectNotFoundError) => NotFound("The project was not found")
        }
    }

  private def deleteProjectEndpoint(projectService: ProjectService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case DELETE -> Root / "projects" / LongVar(id) =>
        for {
          _ <- projectService.delete(id)
          resp <- Ok()
        } yield resp
    }

  private def listProjectsEndpoint(projectService: ProjectService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "projects" :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(offset) =>
        for {
          retrieved <- projectService.list(pageSize.getOrElse(10), offset.getOrElse(0))
          resp <- Ok(retrieved.asJson)
        } yield resp
    }

  private def findProjectsByStatusEndpoint(projectService: ProjectService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "projects" / "findByStatus" :? StatusMatcher(Valid(Nil)) =>
        // User did not specify any statuses
        BadRequest("status parameter not specified")

      case GET -> Root / "projects" / "findByStatus" :? StatusMatcher(Valid(statuses)) =>
        // We have a list of valid statuses, find them and return
        for {
          retrieved <- projectService.findByStatus(NonEmptyList.fromListUnsafe(statuses))
          resp <- Ok(retrieved.asJson)
        } yield resp
    }

  private def findProjectsByBaseEndpoint(projectService: ProjectService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "projects" / "findByBases" :? BaseMatcher(Valid(Nil)) =>
        BadRequest("base iri parameter not specified")

      case GET -> Root / "projects" / "findByBases" :? BaseMatcher(Valid(bases)) =>
        for {
          retrieved <- projectService.findByBase(NonEmptyList.fromListUnsafe(bases))
          resp <- Ok(retrieved.asJson)
        } yield resp

    }





  def endpoints(projectService: ProjectService[F]): HttpRoutes[F] =
      createProjectEndpoint(projectService) <+>
      getProjectEndpoint(projectService) <+>
      deleteProjectEndpoint(projectService) <+>
      listProjectsEndpoint(projectService) <+>
      findProjectsByStatusEndpoint(projectService) <+>
      updateProjectEndpoint(projectService) <+>
      findProjectsByBaseEndpoint(projectService) <+>
        yamlSwaggerEndpoint
}

object ProjectEndpoints {

  def endpoints[F[_]: Effect:ContextShift](projectService: ProjectService[F]): HttpRoutes[F] =
    new ProjectEndpoints[F].endpoints(projectService)

}

