package ubiqore.takibi.doobiedou

import java.sql.Date

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import io.circe._
import io.circe.jawn._
import io.circe.parser.{parse => cparse}
import org.postgresql.util.PGobject

import scala.concurrent.ExecutionContext


case class Medication(index: BigDecimal, sentence: String, mapper: String, result : Json , creationdate : Date )
case class Test(test: String)
case class Response(toto:Json)

object Testing extends App {

  implicit val jsonGet: Get[Json] =
    Get.Advanced.other[PGobject](NonEmptyList.of("json")).tmap[Json] { o =>
      parse(o.getValue).leftMap[Json](throw _).merge
    }

  implicit val jsonPut: Put[Json] =
    Put.Advanced.other[PGobject](NonEmptyList.of("json")).tcontramap[Json] { j =>
      val o = new PGobject
      o.setType("json")
      o.setValue(j.noSpaces)
      o
    }

  implicit val jsonMeta: Meta[Json] =
    Meta.Advanced.other[PGobject]("json").timap[Json](
      a => parse(a.getValue).leftMap[Json](e => throw e).merge)(
      a => {
        val o = new PGobject
        o.setType("json")
        o.setValue(a.noSpaces)
        o
      }
    )

  implicit val cs = IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:ubiqore", "eric", "alig1410"
  )

  def insert1(m: Medication): Update0 = {
    sql"insert into medication ( index, sentence, mapper, result, creationdate ) values (${m.index}, ${m.sentence}, ${m.mapper}, ${m.result}, ${m.creationdate} )".update
  }

  def find2() : ConnectionIO[Option[Response]] ={
    sql"SELECT result-> 'order'->'customer' as toto FROM medication where result-> 'order'->'customer'->>'name' = 'Custy McCustomer'".query[Response].option
  }

  def find(n: String): ConnectionIO[Option[Medication]] =
    sql"select index, sentence, mapper, result, creationdate from medication where sentence = $n".query[Medication].option
 // def find(n: String): ConnectionIO[Option[Test]] = sql"select test from test where test = $n".query[Test].option

  val test: Option[Medication] = find("test").transact(xa).unsafeRunSync()
   val json  = cparse (
     """
        { "order": {
           "customer": {
            "name": "Custy McCustomer",
            "contactDetails": {
                 "address": "1 Fake Street, London, England",
                 "phone": "0123-456-789"
                               }
                        }
                   }
        }
     """

   ).getOrElse(Json.Null)

  val m=Medication(11,"dqsdqdqsdsdqsd","ssdqsdqsd",json,Date.valueOf("2019-02-03"))

 // val test2: Int = insert1(m).run.transact(xa).unsafeRunSync
 // println( test2 )
  val test4: Option[Response] = find2().transact(xa).unsafeRunSync
  test4.map(x=> println(x.toto.toString()))
}


