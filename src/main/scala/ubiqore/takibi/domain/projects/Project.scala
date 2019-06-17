package ubiqore.takibi.domain.projects

import java.time.{ Instant}


case class Project(project_id: Option[Long] = None,
                   project_name : String,
                   project_version_name : String,
                   project_status : ProjectStatus = ProjectStatus.Started,
                   project_description: String,
                   project_start_date   : Instant ,
                   project_fhir_version : String,
                   project_base_iri : String)
