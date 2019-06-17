package ubiqore.takibi

import ca.uhn.fhir.context.{FhirContext, FhirVersionEnum}
import org.hl7.fhir.r4.model.ValueSet
//import org.hl7.fhir.instance.model.api
import org.hl7.fhir.instance.model.api.IBaseResource

object Tools {
  def p(st:String) = println("takibi $$*************"+ st +"*************$$ ibikat")
  val ctx = new FhirContext(FhirVersionEnum.R4)

  def printFhirResource(res: IBaseResource): Unit = {

    p(ctx.newJsonParser ().setPrettyPrint (true).encodeResourceToString(res))

  }

  def stringifyFhirResource(res: IBaseResource): String = {
    ctx.newJsonParser ().setPrettyPrint (true).encodeResourceToString(res)
  }

}

object testTools extends App{
   Tools.printFhirResource(new ValueSet().setCopyright("LIMICS/APHP"))
}