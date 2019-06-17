package ubiqore.takibi.fhir.valuesets.Types

object IRI
{
  def resolve(property:String)(implicit prefixes:Map[String,IRI]): Option[IRI] = property.indexOf(":") match {
    case -1 =>
      //dom.alert(prefixes.toString())
      prefixes.get(":").orElse(prefixes.get("")).map(p=>p / property)
    case 0 =>
      //dom.alert("0"+prefixes.toString())
      prefixes.get(":").orElse(prefixes.get("")).map(p=>p / property)
    case ind=>
      val key = property.substring(0,ind)

      prefixes.get(key).map(p=>p / property).orElse(Some(IRI(property)))

  }
}

/*
implementation of openrdf URI class
 */
case class IRI(uri:String)
{

  def /(child:String): IRI = if
  (stringValue.endsWith("/")
      || stringValue.endsWith("#")
      || stringValue.endsWith("?")
      || stringValue.endsWith("="))
    IRI(stringValue+child)
  else
    IRI(stringValue+ "/" +child) //  IRI( stringValue / child )

  def /(child:IRI): IRI = this / child.stringValue


  require(uri.contains(":"), " iri string must be URI and contain ':' ")


  /**
    * Either "#" or "/"
    */
  lazy val lastSegment = Math.max(uri.lastIndexOf("#"),uri.lastIndexOf("/"))

  lazy val localName: String = uri.substring(Math.min(lastSegment+1,uri.length-1))

  lazy val namespace: String = uri.substring(0,Math.min(lastSegment+1,uri.length-1))

  lazy val label = this.localName.replace("_"," ")

   lazy val stringValue: String = this.uri

  override def toString: String = "<"+uri+">"

  override def hashCode: Int = uri.hashCode

}

object  testIRI extends App{
 println(new IRI("jacky:toto/titi").localName)
  println(new IRI("http://hl7.org/fhir/publication-status").localName)
}



