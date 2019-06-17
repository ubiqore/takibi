package ubiqore.takibi.fhir.valuesets

import java.io.File
import java.util.Date

import kantan.csv._
import kantan.csv.ops._
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus
import org.hl7.fhir.r4.model.{MarkdownType, ValueSet}
import ubiqore.takibi.Tools
import ubiqore.takibi.csv.spec.version1.ValuesetCSV

import scala.annotation.tailrec

case class Code(cs:String,code:String,label:String)
case class VS(id:String, name:String,root:Option[String],codes:Option[List[Code]],version:String)

class ValueSetApi {

  def treatResult[A](l:List[ReadResult[A]]):List[A]={
    l.foldLeft(List[A]())((acc,next)=>if (next.isRight) acc :+ next.right.get else acc )
  }
  def csvFileToData(path:String): List[ValuesetCSV] ={
  //id:String,
  //                     root:Option[String],
  //                     cs:Option[String],
  //                     code:Option[String],
  //                     label:Option[String],
  //                     version:String

    // vs.id	vs.root	codingsystem	code	label (code | vs)	version	spaces 	text|	|text	text|text	xx  | OR |  xx
  //    implicit val vsDecoder: HeaderDecoder[ValuesetCSV] = HeaderDecoder.decoder("id","root","cs","code","label","version")(ValuesetCSV.apply _)
    implicit val vsDecoder: HeaderDecoder[ValuesetCSV] = HeaderDecoder.decoder("id","root","cs","code","label","version")(ValuesetCSV.apply _)

    val uri=getClass.getResource(path).toURI
      val data: List[ReadResult[ValuesetCSV]] =new File(uri).asCsvReader[ValuesetCSV](rfc.withHeader).toList
      val res: List[ValuesetCSV] =treatResult(data)
     // remove doubons

    println("res length="+res.length)
      //return without duplicate by id and code.
    val nodup: List[ValuesetCSV] =res.groupBy( (x=> ( x.id, x.code)  )).map(_._2.head).toList

    println("nodup lenght"+nodup.length)
      nodup
    }



  @tailrec
  final def treat2(l: List[ValuesetCSV],accu:List[VS]): List[VS] ={
    val tmp=l.filter(x=>x.id==l.head.id)

    if (!tmp.isEmpty) {
      val result: VS =tmp.foldLeft(None: Option[VS]) {
        (acc, next) =>
          if (!acc.isDefined){
             val prepare = Some(VS(next.id,next.label.getOrElse(""), next.root, None, next.version))

             addToCodeListOrUpdateName(prepare.get, next)
          }
          else addToCodeListOrUpdateName(acc.get, next)


      }.get
      //val n=result.reverse.head

      val f=VS(result.id,result.name,result.root,result.codes.map(_.reverse),result.version)

      val rest: List[ValuesetCSV] =l.filterNot(x=>tmp.contains(x))

      if(rest.isEmpty) f :: accu
      else  treat2(rest,f :: accu)
    }
    else accu

  }

  @tailrec
  final def treat(l: List[ValuesetCSV],acc:List[VS]): List[VS] ={
    val tmp=l.filter(x=>x.id==l.head.id)
    println(l.length)
    if (!tmp.isEmpty) {
      val result: List[VS] =tmp.foldLeft(List(): List[VS]) {
        (acc, next) =>
          if (acc.isEmpty){
               val prepare = VS(next.id,next.label.getOrElse(""), next.root, None, next.version)
               val vs=  addToCodeList(prepare, next).getOrElse(prepare)
               acc match {
                 case Nil => List(vs)
                 case _ => vs :: acc
                         }
               }

          else
              {
               val last = acc.last
               val newLast = addToCodeList(last, next).getOrElse(acc.last)
               acc:+ newLast
             }

          }
      val n=result.reverse.head
      val f=VS(n.id,n.name,n.root,n.codes.map(_.reverse),n.version)

      val rest: List[ValuesetCSV] =l.filterNot(x=>tmp.contains(x))
      println(rest.toString)

      if(rest.isEmpty) f :: acc
      else  treat(rest,f :: acc)
    }
    else acc

  }


  def addToCodeListOrUpdateName(vs:VS, vscsv:ValuesetCSV): Option[VS] ={

    val newCode:Option[Code]=for {
      cs <- vscsv.cs
      lab <-vscsv.label
      code <-vscsv.code
    }yield (Code(cs,code,lab))

    newCode match {
      case Some(c)=> {
        val codes =vs.codes match {
          case Some(x)=>  c :: x
          case None =>   List(c)
        }

        Some(VS(vs.id,vs.name,vs.root,Some(codes),vs.version))
      }
      // no new code So keep the label
      case None => Some(VS(vs.id,vscsv.label.getOrElse(vs.name),vs.root,vs.codes,vs.version))
    }

  }

  def addToCodeList(vs:VS, vscsv:ValuesetCSV): Option[VS] ={

    val newCode:Option[Code]=for {
      cs <- vscsv.cs
      lab <-vscsv.label
      code <-vscsv.code
    }yield (Code(cs,code,lab))

    newCode match {
      case Some(c)=> {
        val codes =vs.codes match {
          case Some(x)=>  c :: x
          case None =>   List(c)
        }

        Some(VS(vs.id,vs.name,vs.root,Some(codes),vs.version))
      }
      case None => Some(vs)
    }

  }


    def createValueSet() ={
       val vs=new ValueSet()
      vs.setCopyright("LIMICS & APHP")
      val test = new MarkdownType()

      vs.setCopyrightElement(test)
     // vs.setPurposeElement(Markdown)
    }

  def toFhir(vs:VS,date:Date):ValueSet= {
    // usefull
    def sameSystem(a:Code,b:Code):Boolean={a.cs == b.cs}

    val vsf=new ValueSet()
    // http://limics.fr/fhir/ValueSet/ehr2edc/observation-codes.laboratory.ehr2edc
    vsf.setUrl("http://limics.fr/fhir/ValueSet/ehr2edc/"+vs.id+"")
    vsf.setStatus(PublicationStatus.UNKNOWN)
    vsf.setName(vs.name)
    vsf.setDate(date)
    vsf.setCopyright("LIMICS/APHP")
    vsf.setId(vs.id)
    vsf.setVersion(vs.version)

    vs.codes match {
      case Some(cods)=> {
            println("nb Codes= "+cods.length)
            val ordered = cods.sortBy(_.cs)
            val llc=ordered.drop(1).foldLeft(List(List(ordered.head)):List[List[Code]])( (acc,e)=>
               if(sameSystem(e,acc.head.head)) (e::acc.head)::acc.tail
               else List(e)::acc ).map(_.reverse).reverse
           //println(llc)
           val compose=vsf.getCompose.setLockedDate(date).setInactive(false)
          llc.map{ l =>  // llc = List List de codes.
                 val inc=compose.addInclude().setSystem(l.head.cs)
                  l.foreach( code=> inc.addConcept().setCode(code.code).setDisplay(code.label) )
          }
      }
      case None =>
    }
    vsf
  }
  // send updated VS , not everything
  // hier(id,version,root)
  def applyHierarchiy(l:List[ValueSet],hier:List[(String,String,String)],date:Date):List[ValueSet] ={
        hier.foreach { x =>
               val vs =l.filter(vs=>vs.getId == x._3).head
               vs.getCompose.setLockedDate(date)
               val inc=vs.getCompose.getIncludeFirstRep
               inc.addValueSet("http://aphp.fr/fhir/ValueSet/"+x._1+"|"+x._2)  //vsf.setUrl("http://aphp.fr/fhir/ValueSet/"+vs.id+"/>")
                }
        l
  }


}


// observation-category.ehr2edc
object decodeTest extends App{

  val test= new ValueSetApi()
  val l: List[ValuesetCSV] =test.csvFileToData("/medication-form.ehr2edc.csv")
 //val l: List[ValuesetCSV] =test.csvFileToData("/observation-category.ehr2edc.csv") OK
  val res=test.treat2(l,List())

  val date = new Date
  //println(res.toString())
  val myVS= res.map(x=> test.toFhir(x,date))
  val children=res.filter(x => x.root.isDefined)
  val fathers=res.filter(x=>x.root.isEmpty)

  val inter : List[(String, String, String)] = children.map(c=>(c.id,c.version,c.root.get))

  test.applyHierarchiy(myVS,inter,date).foreach(Tools.printFhirResource(_))
  import java.io.PrintWriter
  test.applyHierarchiy(myVS,inter,date).foreach{ x =>
    val str=x.getName.replaceAll(" \\| ","_")
                     .replaceAll("/","_FOR_")
                     .replaceAll(" ","_")
    new PrintWriter("/Users/roky/data/EHR2EDC/3juin/"+ x.getId+":"+ str +".json") { write(Tools.stringifyFhirResource(x)); close }

  }


}