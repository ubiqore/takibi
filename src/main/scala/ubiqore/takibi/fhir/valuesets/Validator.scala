package ubiqore.takibi.fhir.valuesets

import better.files._
import io.circe.{Json, JsonObject, ParsingFailure}
import io.circe.parser.parse
import ubiqore.takibi.fhir.valuesets.Types.IRI

class Validator {
 // base on Structural Definition.

  def openJsonResourceFile(rep:String)(name:String) : Either[ParsingFailure,Json] = {
    println("test d'ouverture de ::> "+rep+"/"+name+".json")
    parse(Resource.asStream(rep+"/"+name+".json").get.asString())
  }
  def openCodingSystem(name:String) =openJsonResourceFile("FHIR/R4/CS")("CodeSystem_"+name)
  def openStructuralDefinition(name:String) =openJsonResourceFile("FHIR/R4/SD")(name)
  def openValueSet(name:String) =openJsonResourceFile("FHIR/R4/VS")("Valueset_"+ name)
}

object AppValidator extends App {
  val test= new Validator
  test.openValueSet("publication-status").map{
     json =>
       json.hcursor.downField("resource").downField("compose").downField("include").as[Seq[JsonObject]].right.get.foreach{
          jo=> val iri = new IRI(jo("system").get.asString.get)
              println("iri.localname="+iri.localName)
              test.openCodingSystem(iri.localName) . map {
              json2 =>
                val data=json2.hcursor.downField("resource").downField("concept").as[Seq[JsonObject]].right.get
                data.foreach(jo=> println(jo.keys.toList))
            }

       }


  }
}

object TestStructuralDef extends App{

  val test = new Validator
  test.openStructuralDefinition("ValueSet").map {
    json => json.hcursor.downField("resource").downField("snapshot").downField("element").as[Seq[JsonObject]].right.get.foreach{
      jo=>  if (jo("min").get.as[Int].right.get == 1){println(jo("id").get.as[String].right.get)}
    }

  }
}