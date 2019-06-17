package ubiqore.takibi.domain.projects

import scala.language.higherKinds

import cats.data.EitherT
import ubiqore.takibi.domain.{ProjectAlreadyExistsError, ProjectNotFoundError}

trait ProjectValidationAlgebra[F[_]] {

  /* Fails with a PetAlreadyExistsError */
  def doesNotExist(project: Project): EitherT[F, ProjectAlreadyExistsError, Unit]

  /* Fails with a PetNotFoundError if the pet id does not exist or if it is none */
  def exists(projectId: Option[Long]): EitherT[F, ProjectNotFoundError.type, Unit]
}
