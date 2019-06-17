package ubiqore.takibi.fhir.services

import cats.effect._
import cats.implicits._
import io.circe.Json
import io.circe.generic.auto._
import org.http4s.Uri
import org.http4s.Status.{NotFound, Successful}
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

object ClientExample extends IOApp {

  def getSite(client: Client[IO]): IO[Unit] = IO {
    val page: IO[Json] = client.expect[Json](Uri.uri("http://houblon.limics.upmc.fr/omop/vocabulary"))

    println(page.map(_.asArray).unsafeRunSync()) // each execution of the effect will refetch the page!

    // We can do much more: how about decoding some JSON to a scala object
    // after matching based on the response status code?

    final case class Foo(bar: String)

    // Match on response code!
    val page2 = client.get(Uri.uri("http://http4s.org/resources/foo.json")) {
      case Successful(resp) =>
        // decodeJson is defined for Json4s, Argonuat, and Circe, just need the right decoder!
        resp.decodeJson[Foo].map("Received response: " + _)
      case NotFound(_) => IO.pure("Not Found!!!")
      case resp => IO.pure("Failed: " + resp.status)
    }

    println(page2.unsafeRunSync())
  }

  def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](global).resource
      .use(getSite)
      .as(ExitCode.Success)
}
