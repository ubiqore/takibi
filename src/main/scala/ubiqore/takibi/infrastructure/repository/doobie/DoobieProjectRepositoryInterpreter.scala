package ubiqore.takibi.infrastructure.repository.doobie


import cats._
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import ubiqore.takibi.domain.projects.{Project, ProjectRepositoryAlgebra,ProjectStatus}
import SQLPagination._




private object ProjectSQL {

  /* We require type StatusMeta to handle our ADT Status */
  implicit val StatusMeta: Meta[ProjectStatus] =
    Meta[String].imap(ProjectStatus.withName)(_.entryName)

  /* This is used to marshal our sets of strings */
  implicit val SetStringMeta: Meta[Set[String]] =
    Meta[String].imap(_.split(',').toSet)(_.mkString(","))


  def insert(p: Project): Update0 =
    sql"""
           INSERT INTO project
           ( project_name, project_version_name, project_status, project_description, project_start_date, project_fhir_version, project_base_iri    )
           values (${p.project_name}, ${p.project_version_name}, ${p.project_status}, ${p.project_description}, ${p.project_start_date} , ${p.project_fhir_version},  ${p.project_base_iri} )
          RETURNING project_id
       """.update



 // update NOM , VERSION, DESCRIPTION
  def update(project:Project, id: Long) : Update0 = sql"""
    UPDATE project
    SET project_name = ${project.project_name}, project_version_name = ${project.project_version_name}, project_description = ${project.project_description}
    WHERE project_id = $id
  """.update

  def select(id: Long) : Query0[Project] = sql"""
    SELECT project_id, project_name, project_version_name, project_status, project_description, project_start_date, project_fhir_version, project_base_iri
    FROM Project
    WHERE project_id = $id
  """.query

  def delete(id: Long) : Update0 = sql"""
    DELETE FROM project WHERE project_id = $id
  """.update

  def selectByNameAndFhirVersion(name: String, fhir_version: String) : Query0[Project] = sql"""
    SELECT project_id, project_name, project_version_name, project_status, project_description, project_start_date, project_fhir_version, project_base_iri
    FROM project
    WHERE project_name = $name AND project_fhir_version = $fhir_version
  """.query[Project]

  def selectAll : Query0[Project] = sql"""
    SELECT project_id, project_name, project_version_name, project_status, project_description, project_start_date, project_fhir_version, project_base_iri
    FROM  project
    ORDER BY project_start_date desc
  """.query

  def selectByStatus(statuses: NonEmptyList[ProjectStatus]) : Query0[Project] = (
    sql"""
      SELECT project_id project_name, project_version_name, project_status, project_description, project_start_date, project_fhir_version, project_base_iri
      FROM project
      WHERE """ ++ Fragments.in(fr"STATUS", statuses)
    ).query

  def selectBaseIRILikeString(basesIRI: NonEmptyList[String]) : Query0[Project] = {
    /* Handle dynamic construction of query based on multiple parameters */

    /* To piggyback off of comment of above reference about tags implementation, findByTag uses LIKE for partial matching
    since tags is (currently) implemented as a comma-delimited string */
    val basesIRILikeString: String = basesIRI.toList.mkString("base_iri LIKE '%", "%' OR base_iri LIKE '%", "%'")

    (sql""" SELECT project_name, project_version_name, project_status, project_description, project_start_date, project_fhir_version, project_base_iri
          FROM project
         WHERE """ ++ Fragment.const(basesIRILikeString))
      .query[Project]
  }
}

class DoobieProjectRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
  extends ProjectRepositoryAlgebra[F] {
  import ProjectSQL._

  def create(project:Project): F[Project] = {

    insert(project).withUniqueGeneratedKeys[Long]("project_id").map(project_id => project.copy(project_id = project_id.some)).transact(xa)
  }
  def update(project:Project): F[Option[Project]] = OptionT.fromOption[ConnectionIO](project.project_id).semiflatMap(id =>
    ProjectSQL.update(project, id).run.as(project)
  ).value.transact(xa)

  def get(id: Long): F[Option[Project]] = select(id).option.transact(xa)

  def delete(id: Long): F[Option[Project]] = OptionT(get(id)).semiflatMap(project =>
   ProjectSQL.delete(id).run.transact(xa).as(project)
  ).value

  def findByNameAndFhirVersion(name: String, fhir_version: String): F[Set[Project]] =
    selectByNameAndFhirVersion(name, fhir_version).to[List].transact(xa).map(_.toSet)

  def list(pageSize: Int, offset: Int): F[List[Project]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  def findByStatus(statuses: NonEmptyList[ProjectStatus]): F[List[Project]] =
    selectByStatus(statuses).to[List].transact(xa)

  def findByBaseIRI(bases: NonEmptyList[String]): F[List[Project]] =
    selectBaseIRILikeString(bases).to[List].transact(xa)
}

object DoobieProjectRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieProjectRepositoryInterpreter[F] =
    new DoobieProjectRepositoryInterpreter(xa)
}
