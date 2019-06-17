package ubiqore.takibi.domain.profiles

import java.time.Instant

import io.circe.Json

case class Profile (profile_id: Option[Long],
                    profile_identity : String,
                    profile_name : String,
                    profile_start_date : Instant,
                    profile_flag : String,
                    profile_content : Json,
                    profile_valid : Int,
                    profile_project_id :  Int,
                    profile_maturity : Int

                   )
