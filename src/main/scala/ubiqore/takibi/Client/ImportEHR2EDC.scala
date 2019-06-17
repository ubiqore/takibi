package ubiqore.takibi.Client
import java.io.File
import java.time.Clock

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import io.circe.Json
import io.circe.jawn.parse
import org.postgresql.util.PGobject
import ubiqore.takibi.domain.profiles.Profile
import ubiqore.takibi.domain.valuesets.ValueSet

import scala.concurrent.ExecutionContext
import scala.io.Source

object importVS extends App {

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
    "org.postgresql.Driver", "jdbc:postgresql://localhost:5433/takibi", "takibi", "bonfire"
  )
  val clock= Clock.systemDefaultZone()
  val dir="/Users/roky/data/EHR2EDC/3juin/"
  val dirLab=dir+"vs/vitals"

  val labList=getListOfFiles(dirLab)
  labList.filter(_.getName.endsWith("json")).sortBy(_.getName).zipWithIndex.foreach {
    case (vs, index) =>
    println( (index+1)+" "+ vs.getName)
    val vs_json_string = Source.fromFile(vs.getAbsolutePath).getLines.mkString
    val json:Json = parse(vs_json_string).getOrElse(Json.Null)
   // println(json)
    println(json.hcursor.downField("resourceType").as[String])

   val onchoisitkoi="titi"
   if (onchoisitkoi=="pro")
      generateProfiles()
   else generateVS()

  def generateVS()= {

      val vs_db = new ValueSet(None, json.hcursor.downField("id").as[String].right.get,
        json.hcursor.downField("name").as[String].right.get,
        clock.instant(), "demo", json, 0, 1, 0)

      val process: IO[Int] = insertValueSet(vs_db).run.transact(xa)
      process.attempt.map(x => x match {
        case Left(t) => println(t.getMessage)
        case Right(i) => println("ok bien passé ==> " + i)
      }).unsafeRunSync()

    }

    def generateProfiles() = {
      println("yehe")
     val profile_db = new Profile(None, json.hcursor.downField("url").as[String].right.get,
        json.hcursor.downField("name").as[String].right.get,
        clock.instant(), "demo", json, 0, 1, 0)


      val process2: IO[Int] = insertProfile2(profile_db).run.transact(xa)
      process2.attempt.map(x => x match {
        case Left(t) =>
          println("error==> "+t.getMessage)
        case Right(i) => println("ok bien passé ==> " + i)
      }).unsafeRunSync()
    }


  }



  def insertValueSet(vs: ValueSet): Update0 = {
    sql"""
          insert into valueset

           (    valueset_identity, valueset_name,
                valueset_start_date, valueset_flag ,valueset_content,
                valueset_valid, valueset_project_id , valueset_maturity)

           values (${vs.valueset_identity}, ${vs.valueset_name},
                  ${vs.valueset_start_date}, ${vs.valueset_flag}, ${vs.valueset_content} ,
                  ${vs.valueset_valid},  ${vs.valueset_project_id} , ${vs.valueset_maturity} )
       """.update
  }


  def insertProfile2(pr: Profile): Update0 = {
    sql"""
           insert into profile
           ( profile_identity, profile_name ,
             profile_start_date , profile_flag , profile_content ,
             profile_valid , profile_project_id  , profile_maturity)
           values (${pr.profile_identity}, ${pr.profile_name},
                    ${pr.profile_start_date}, ${pr.profile_flag}, ${pr.profile_content} ,
                    ${pr.profile_valid},  ${pr.profile_project_id} , ${pr.profile_maturity})

       """.update

  }


  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

}
