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
import org.http4s.{EntityDecoder, HttpRoutes }  // QueryParamDecoder

import scala.language.higherKinds
import ubiqore.takibi.domain.{ValueSetAlreadyExistsError, ValueSetNotFoundError}
import ubiqore.takibi.domain.valuesets.{ValueSet, ValueSetService}

class ValueSetEndpoints[F[_]: Effect] extends Http4sDsl[F] {

  //  import Pagination._



  /* Parses out tag query param, which could be multi-value */
  object BaseMatcher extends OptionalMultiQueryParamDecoderMatcher[String]("bases")

  /* Need Instant Json Encoding */
  import io.circe.java8.time._

  implicit val projectDecoder: EntityDecoder[F, ValueSet] = jsonOf[F, ValueSet]


  private def createValueSetEndpoint(valueSetService: ValueSetService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "valuesets" =>
        val action = for {
          vs <- req.as[ValueSet]
          result <- valueSetService.create(vs).value
        } yield result

        action.flatMap {
          case Right(saved) =>
            Ok(saved.asJson)
          case Left(ubiqore.takibi.domain.ValueSetWrongJsonError(existing)) =>
            BadRequest(s"The valueset ${existing.valueset_identity} has wrong json as content")
          case Left(ValueSetAlreadyExistsError(existing)) =>
            Conflict(s"The valueset ${existing.valueset_identity} of maturity ${existing.valueset_maturity} already exists")
          case _ => BadRequest("strange case error / contact admin.")
        }
    }

  private def updateValueSetEndpoint(valueSetService: ValueSetService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ PUT -> Root / "valuesets" / LongVar(valueSetId) =>
        val action: F[Either[ValueSetNotFoundError.type, ValueSet]] = for {
          vs <- req.as[ValueSet]
          updated = vs.copy(valueset_id = Some(valueSetId))
          result <- valueSetService.update(updated).value
        } yield result

        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(ValueSetNotFoundError) => NotFound("The valueSet was not found")
        }
    }

  private def readValueSetEndpoint(valueSetService: ValueSetService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "valuesets" / LongVar(id) =>
        valueSetService.read(id).value.flatMap {
          case Right(found) => Ok(found.asJson)
          case Left(ValueSetNotFoundError) => NotFound("The valueSet was not found")
        }
    }

  private def deleteValueSetEndpoint(valueSetService: ValueSetService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case DELETE -> Root / "valuesets" / LongVar(id) =>
        for {
          _ <- valueSetService.delete(id)
          resp <- Ok()
        } yield resp
    }



  def endpoints(valuesetService: ValueSetService[F]): HttpRoutes[F] =
      createValueSetEndpoint(valuesetService) <+>
      readValueSetEndpoint(valuesetService) <+>
      deleteValueSetEndpoint(valuesetService) <+>
      updateValueSetEndpoint(valuesetService)

}

object ValueSetEndpoints {
  def endpoints[F[_]: Effect](valueSetService: ValueSetService[F]): HttpRoutes[F] =
    new ValueSetEndpoints[F].endpoints(valueSetService)
}

