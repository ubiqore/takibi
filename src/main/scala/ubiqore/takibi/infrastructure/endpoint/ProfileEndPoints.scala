package ubiqore.takibi.infrastructure.endpoint

//import cats.data.Validated.Valid
//import cats.data._
import cats.effect.Effect
import cats.implicits._
//import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import ubiqore.takibi.domain.profiles.{Profile, ProfileService}
import ubiqore.takibi.domain.{ProfileAlreadyExistsError, ProfileNotFoundError}

import scala.language.higherKinds

class ProfileEndpoints[F[_]: Effect] extends Http4sDsl[F] {

  //  import Pagination._



  /* Parses out tag query param, which could be multi-value */
  object BaseMatcher extends OptionalMultiQueryParamDecoderMatcher[String]("bases")

  /* Need Instant Json Encoding */

  implicit val profileDecoder: EntityDecoder[F, Profile] = jsonOf[F, Profile]


  private def createProfileEndpoint(profileService: ProfileService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "profiles" =>
        val action = for {
          vs <- req.as[Profile]
          result <- profileService.create(vs).value
        } yield result

        action.flatMap {
          case Right(saved) =>
            Ok(saved.asJson)
          case Left(ubiqore.takibi.domain.ProfileWrongJsonError(existing)) =>
            BadRequest(s"The profile ${existing.profile_identity} has wrong json as content")
          case Left(ProfileAlreadyExistsError(existing)) =>
            Conflict(s"The profile ${existing.profile_identity} of maturity ${existing.profile_maturity} already exists")
          case _ => BadRequest("strange case error / contact admin.")
        }
    }

  private def updateProfileEndpoint(profileService: ProfileService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ PUT -> Root / "profiles" / LongVar(profileId) =>
        val action: F[Either[ProfileNotFoundError.type, Profile]] = for {
          pr <- req.as[Profile]
          updated = pr.copy(profile_id = Some(profileId))
          result <- profileService.update(updated).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(ProfileNotFoundError) => NotFound("The profile was not found")
        }
    }

  private def readProfileEndpoint(profileService: ProfileService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "profiles" / LongVar(id) =>
        profileService.read(id).value.flatMap {
          case Right(found) => Ok(found.asJson)
          case Left(ProfileNotFoundError) => NotFound("The profile was not found")
        }
    }

  private def deleteProfileEndpoint(profileService: ProfileService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case DELETE -> Root / "profiles" / LongVar(id) =>
        for {
          _ <- profileService.delete(id)
          resp <- Ok()
        } yield resp
    }



  def endpoints(profileService: ProfileService[F]): HttpRoutes[F] =
      createProfileEndpoint(profileService) <+>
      readProfileEndpoint(profileService) <+>
      deleteProfileEndpoint(profileService) <+>
      updateProfileEndpoint(profileService)

}

object ProfileEndpoints {
  def endpoints[F[_]: Effect](profileService: ProfileService[F]): HttpRoutes[F] =
    new ProfileEndpoints[F].endpoints(profileService)
}

