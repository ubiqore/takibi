package ubiqore.takibi.fhir.validation

import scala.util.Try

case class UnsignedInt(int:Int){

}

case class Max(int:Option[UnsignedInt], str:Option[String])

case class Cardinality(min:UnsignedInt , max:Max){
  if (max.int.isDefined) {
    if (min.int > max.int.get.int) throw new IllegalArgumentException("min must be equal or lower than max")
  }
}


// Cardinality Rules
// 1..1
// 0..* => 1..*  , 2

case class CardinalityOperations (){

  def getUnsignedIntOrNone(value: Int): Option[UnsignedInt] =
    value match {
      case x if x >= 0 => Some(new UnsignedInt(x));
      case _ => None
    }


  def getMin(value: Int): Option[UnsignedInt] = {
    getUnsignedIntOrNone(value)
  }

  def getMax(value: String): Option[Max] = {
    val int: Option[UnsignedInt] = Try {
      getUnsignedIntOrNone(value.trim.toInt).get
    }.toOption
    int match {
      case Some(i) => Some(Max(Some(i), None))
      case None => value.trim match {
        case "*" => Some(Max(None, Some("*")));
        case _ => None
      }
    }
  }


  def createCardinality(min: Int, max: String): Option[Cardinality] = {
    Try {
      new Cardinality(getMin(min).get, getMax(max).get)
    }.toOption
  }

  def checkIntegrity(cNew: Cardinality, cFather: Cardinality): Boolean = {

    val MaxIntegrity = cFather.max.int match {
      case Some(iOld) => cNew.max.int match {
        case None => false
        case Some(iNew) => iOld.int - iNew.int <= 0 // Old = 5 & New = 6 => Wrong for example
      }
      case _ => true // * case , so integrity is always OK.
    }

    // Old : 0  , New : 1
    val MinIntegrity = cFather.min.int <= cNew.min.int

    //
    MaxIntegrity && MinIntegrity
  }

}

object TestIngCardinalityIntegrity extends App {
  val ope=CardinalityOperations()
  println(ope.getUnsignedIntOrNone(4))
  println(ope.getUnsignedIntOrNone(-1))

var o=ope.createCardinality(1,"1")
var n=ope.createCardinality(2,"1")
  o.map(oldo => n.map(newo=> println(ope.checkIntegrity(newo,oldo))))

  o=ope.createCardinality(1,"1")
  n=ope.createCardinality(1,"1")
  o.map(oldo => n.map(newo=> println(ope.checkIntegrity(newo,oldo))))

  val test1: Option[Boolean] = for {
    o <- ope.createCardinality(1,"1")
    n <- ope.createCardinality(2,"1")

  }yield (ope.checkIntegrity(n,o))
  if (test1.isDefined)println("test 1 ="+test1.get)
  else println("Cardinality could not be created / issue with rules")
  o=ope.createCardinality(1,"1")
  n=ope.createCardinality(1,"*")
  o.map(oldo => n.map(newo=> println(ope.checkIntegrity(newo,oldo))))


  o=ope.createCardinality(0,"*")
  n=ope.createCardinality(1,"*")
  o.map(oldo => n.map(newo=> println(ope.checkIntegrity(newo,oldo))))

  o=ope.createCardinality(0,"*")
  n=ope.createCardinality(1,"1")
  o.map(oldo => n.map(newo=> println(ope.checkIntegrity(newo,oldo))))

  o=ope.createCardinality(0,"1")
  n=ope.createCardinality(0,"n")
  o.map(oldo => n.map(newo=> println("should b = "+ope.checkIntegrity(newo,oldo))))
}

