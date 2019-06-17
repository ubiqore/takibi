
DROP TABLE IF EXISTS project cascade;

DROP TABLE IF EXISTS valueset cascade ;
DROP TABLE IF EXISTS profile cascade;
DROP TABLE IF EXISTS fhir_version cascade;

create table project (
   -- internal (technical) identification of a project
  project_id			bigserial		primary key  ,
  project_name			varchar(255) 		not null unique ,
   -- project version name (free strategy / not technical)
  project_version_name			varchar(255)		not null ,
   -- status [TODO : use Status tag to freeze a project]
  project_status		varchar(20)		not null ,

  project_description		text			not null ,

  project_start_date		date			not null ,
   -- FHIR version (dstu3, r4)
  project_fhir_version                  varchar(20)             not null ,

  project_base_iri			text			not null
 )
;


 
create table valueset (
  valueset_id 			    bigserial		  primary key ,
  valueset_identity			text		  	  not null ,
  valueset_name			    varchar(255)	not null ,
  valueset_start_date		date			    not null ,
  valueset_flag			    varchar(10)		not null ,
  valueset_content 			json 			    not null ,
  valueset_valid  			integer			  not null ,
  valueset_project_id		bigint		  	not null,
  valueset_maturity     bigint        not null
)
;

create table profile (
  profile_id 			    bigserial		  primary key ,

  profile_identity		text		  	  not null ,
  profile_name			    varchar(255)	not null ,
  profile_start_date		date			    not null ,
  profile_flag			    varchar(10)		not null ,
  profile_content 			json 			    not null ,
  profile_valid  			integer			  not null ,
  profile_project_id		bigint		  	not null,
  profile_maturity     bigint        not null
)
;

COMMENT ON COLUMN profile.profile_id IS 'technical (internal) identification of a profile [No relationship with FHIR ids]  ';
COMMENT ON COLUMN profile.profile_identity IS 'identification of a profile that corresponds to FHIR[profile.name] ';
create table fhir_version (
   version_name  		varchar(255) 		not null unique,
   version_id				bigserial  	 	  primary key
)
;


