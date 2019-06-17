package ubiqore.takibi.domain.projects

import scala.language.higherKinds

import cats.data.NonEmptyList

trait ProjectRepositoryAlgebra[F[_]] {

  def create(project: Project): F[Project]

  def update(project: Project) : F[Option[Project]]

  def get(id: Long): F[Option[Project]]

  def delete(id: Long): F[Option[Project]]

  def findByNameAndFhirVersion(name: String, fhir_version: String): F[Set[Project]]

  def list(pageSize: Int, offset: Int): F[List[Project]]

  def findByStatus(status: NonEmptyList[ProjectStatus]): F[List[Project]]

  def findByBaseIRI(bases: NonEmptyList[String]): F[List[Project]]
}




