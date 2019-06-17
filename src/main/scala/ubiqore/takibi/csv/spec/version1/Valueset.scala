package ubiqore.takibi.csv.spec.version1

/**
  * Valueset CSV Column specification
  * version 1 .
  *
  */
//
// vs.name	vs.root	codingsystem	code	label	version
case class ValuesetCSV (id:String,
                     root:Option[String],
                     cs:Option[String],
                     code:Option[String],
                     label:Option[String],
                     version:String
                    )


