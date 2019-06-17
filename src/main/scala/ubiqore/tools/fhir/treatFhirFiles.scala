package ubiqore.tools.fhir

import java.time.Clock

import better.files._
import File._
import io.circe.{ACursor, Json}
import io.circe.parser._

class treatFhirFiles () {


  def test1()  ={
  val clock: Long =Clock.systemDefaultZone().millis()
  val defs= home / "FHIR" / "R4" / "definitions"
  val out: File = defs / ("out"+clock)
  out.createDirectoryIfNotExists()

 //val profile_type = defs / "profiles-types.json"
 //val profile_resource = defs / "profiles-resources.json"
 val valuesets = defs / "valuesets.json"
 //  val codingsys = defs / "v3-codesystems.json"
 //val v2_tables = defs / "v2-tables.json"
  val json:Json = parse(valuesets.contentAsString).getOrElse(Json.Null)


    println(json.hcursor.downField("resourceType").as[String])

  json.hcursor.downField("entry").as[Set[Json]].map(
      x=> x.foreach{
         p=>
            val prefix:String = p.hcursor.downField("resource").downField("resourceType").as[String].right.get

            val name:String = p.hcursor.downField("resource").downField("name").as[String].right.get.replaceAll("/","_")
           (out / (prefix + "_" + name + ".json") ).createFileIfNotExists().overwrite(removeTextField(p).toString())
   } )


}

  def removeTextField(j : Json ):Json = {
    println("test= "+ j.hcursor.downField("resource").downField("name").as[String].right.get )

  //  val reversedNameCursor: ACursor = j.hcursor.downField("resource").downField("text").downField("div").withFocus(_.mapString(_=>""))
  val reversedNameCursor: ACursor = j.hcursor.downField("resource").downField("text").downField("div").withFocus(_.mapString(_=>""))

    reversedNameCursor.top.get
  }

}


object app444 extends App {
  val fhirPath=""

  val test = new treatFhirFiles()
  test.test1()
}