package ubiqore.takibi.db.access

import java.sql.Date
import java.time.Clock
import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import io.circe._
import io.circe.jawn._
import io.circe.parser.{parse => cparse}
import org.postgresql.util.PGobject
import ubiqore.takibi.domain.projects.{Project, ProjectStatus}

import scala.concurrent.ExecutionContext


case class ValueSet(id: Option[Long],
                    identity : String,
                    name : String,
                    date : Date,
                    flag : String,
                    content : Json,
                    valid : Int,
                    project_id :  Int)

case class FhirVersion(name : String, id :Option[Long])



case class Response(toto:Json)

object TestingXXX extends App {

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

  implicit val StatusMeta: Meta[ProjectStatus] =
    Meta[String].imap(ProjectStatus.withName)(_.entryName)

  implicit val cs = IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql://localhost:5433/takibi", "takibi", "bonfire"
  )

  def insertProject(p: Project): Update0 = {
    sql"""
          insert into project

           ( project_name, project_version_name, project_status, project_description, project_start_date, project_fhir_version, project_base_iri    )

           values (${p.project_name}, ${p.project_version_name}, ${p.project_status}, ${p.project_description}, ${p.project_start_date} , ${p.project_fhir_version},  ${p.project_base_iri} )
       """.update
  }

  def insertValueSet(vs: ValueSet): Update0 = {
    sql"""
          insert into valueset

           (   vs_identitiy, vs_name, vs_date, vs_flag ,vs_content, vs_valid, project_id )

           values (${vs.identity}, ${vs.name}, ${vs.date}, ${vs.flag}, ${vs.content} , ${vs.valid},  ${vs.project_id} )
       """.update
  }
  val clock= Clock.systemDefaultZone()
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
  val p=Project(None,"ehr2edc","v0.001",ProjectStatus.Started,"A project to test DB",clock.instant(),"dstu3", "http://limics.fr/ehr2edc" )
  insertProject(p).run.transact(xa)
            .attempt.map(x => x match {
              case Left(t) => println(t.getMessage)
              case Right(i) => println("ok insert project bien passé ==> "+ i)
              }).unsafeRunSync()
  //val vs=ValueSet(None,"vsTest","Testing",Date.valueOf("2019-02-03"),"test",json,0,1)
  //val test: IO[Int] = insertValueSet(vs).run.transact(xa)
  /**test.attempt.map(x => x match {
                            case Left(t) => println(t.getMessage)
                            case Right(i) => println("ok bien passé ==> "+ i)
                                }).unsafeRunSync()

   **/
  //insert(order).withUniqueGeneratedKeys[Long]("ID").map(id => order.copy(id = id.some)).transact(xa)

}


