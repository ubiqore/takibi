package ubiqore.takibi.fhir.services

import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity
import org.hl7.fhir.r4.model._
import ubiqore.takibi.fhir.valuesets.Types.IRI
// import org.http4s.client.blaze._
// import org.http4s.client._
case class Concept(code:String,display:String,system:String,version:String)
// implicit Rules : code+system OR coding (with code + system) ARE REQUIRED
abstract class CodeSystem {
 def $lookup( codeO: Option[String] ,
              systemO:Option[IRI],
              versionO:Option[String],
              codingO:Option[Coding] ,
              dateO : Option[DateTimeType] ,
              displayLanguageO : Option[String] ,
              propertyO : Option[List[String]]) : Either[OperationOutcome,Parameters]

}

class CodeSystemOMOP extends CodeSystem {

  override def $lookup(codeO: Option[String], systemO: Option[IRI], versionO: Option[String], codingO: Option[Coding], dateO: Option[DateTimeType], displayLanguageO: Option[String], propertyO: Option[List[String]]): Either[OperationOutcome, Parameters] = {

    val codingOO= this.getCodingFromCodeAndSystem(codeO,systemO).getOrElse(this.getCodingFromCoding(codingO))
    codingOO match {
      case None =>  Left(issueNoGoodInput)
      case Some(_)  => Left(issueNoGoodInput)
    }


  }


  def getCodingFromCodeAndSystem(codeO : Option[String], systemO:Option[IRI]):Option[Coding]={
     if (codeO.isDefined && systemO.isDefined) Some( new Coding().setCode(codeO.get).setSystem(systemO.get.toString))
     else None
  }
  def getCodingFromCoding(codingO:Option[Coding]) : Option[Coding] ={
    if (codingO.isDefined){
      val coding=codingO.get
      if (coding.getCode ==null) None
      else if (coding.getSystem== null)  None
      else codingO
    }
    else None
  }

  val issueNoGoodInput : OperationOutcome = {
    val op=new OperationOutcome
    op.setId("exception")
    op.addIssue().setSeverity(IssueSeverity.ERROR).setCode(OperationOutcome.IssueType.REQUIRED).setDetails(new CodeableConcept().setText("Input Code/System OR Coding (with code & system) are missing"))
    op
  }

  def issueCodeNotfound(str :String) : OperationOutcome = {
    val op=new OperationOutcome
    op.setId("exception")
    op.addIssue().setSeverity(IssueSeverity.ERROR).setCode(OperationOutcome.IssueType.NOTFOUND).setDetails(new CodeableConcept().setText(s"No result for Input :  $str not "))
    op
  }








}

