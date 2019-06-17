package ubiqore.takibi.domain.valuesets

import java.time.Instant

import io.circe.Json

case class ValueSet(valueset_id: Option[Long],
                    valueset_identity : String,
                    valueset_name : String,
                    valueset_start_date : Instant,
                    valueset_flag : String,
                    valueset_content : Json,
                    valueset_valid : Int,
                    valueset_project_id :  Int,
                    valueset_maturity : Int)
