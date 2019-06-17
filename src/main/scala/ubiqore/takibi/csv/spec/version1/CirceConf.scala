package ubiqore.takibi.csv.spec.version1


import cats.implicits._
import io.circe.Json
import io.circe.parser._

object  test extends App {
  val one = Option(1)
  val two = Option(2)
  val n: Option[Int] = None

  val testt: Option[Int] = one <+> two




  val test = "{ \"titi\" : \"toto\" }"
  val test2 =decode[Json](test)
  println(test2.toString)
}