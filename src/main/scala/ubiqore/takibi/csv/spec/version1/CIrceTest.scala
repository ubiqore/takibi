package ubiqore.takibi.csv.spec.version1
import  io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._


sealed trait Foo
case class Bar(xs: Vector[String]) extends Foo
case class Qux(i: Int, d: Option[Double]) extends Foo

object ttest extends App {
  val foo: Foo = Qux (13, Some (14.0) )

  val json = foo.asJson.noSpaces
  println (json)

  val decodedFoo = decode[Foo] (json)
  println (decodedFoo)

}