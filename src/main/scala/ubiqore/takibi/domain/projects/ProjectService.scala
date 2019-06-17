package ubiqore.takibi.domain.projects



import scala.language.higherKinds

import cats._
import cats.data._
import ubiqore.takibi.domain.{ProjectAlreadyExistsError, ProjectNotFoundError}

/**
  * The entry point to our domain, works with repositories and validations to implement behavior
  * @param repository where we get our data
  * @param validation something that provides validations to the service
  * @tparam F - this is the container for the things we work with, could be scala.concurrent.Future, Option, anything
  *           as long as it is a Monad
  */
class ProjectService[F[_]](repository: ProjectRepositoryAlgebra[F], validation: ProjectValidationAlgebra[F]) {
  import cats.syntax.all._

  def create(project: Project)(implicit M: Monad[F]): EitherT[F, ProjectAlreadyExistsError, Project] = for {
    _ <- validation.doesNotExist(project)
    saved <- EitherT.liftF(repository.create(project))
  } yield saved

  /* Could argue that we could make this idempotent on put and not check if the project exists */
  def update(project: Project)(implicit M: Monad[F]): EitherT[F, ProjectNotFoundError.type, Project] = for {
    _ <- validation.exists(project.project_id)
    saved <- EitherT.fromOptionF(repository.update(project), ProjectNotFoundError)
  } yield saved

  def get(id: Long)(implicit M: Monad[F]): EitherT[F, ProjectNotFoundError.type, Project] =
    EitherT.fromOptionF(repository.get(id), ProjectNotFoundError)

  /* In some circumstances we may care if we actually delete the project; here we are idempotent and do not care */
  def delete(id: Long)(implicit M: Monad[F]): F[Unit] =
    repository.delete(id).as(())

  def list(pageSize: Int, offset: Int): F[List[Project]] =
    repository.list(pageSize, offset)

  def findByStatus(statuses: NonEmptyList[ProjectStatus]): F[List[Project]] =
    repository.findByStatus(statuses)

  def findByBase(bases: NonEmptyList[String]): F[List[Project]] =
    repository.findByBaseIRI(bases)
}

object ProjectService {
  def apply[F[_]: Monad](repository: ProjectRepositoryAlgebra[F], validation: ProjectValidationAlgebra[F]) =
    new ProjectService[F](repository, validation)
}

