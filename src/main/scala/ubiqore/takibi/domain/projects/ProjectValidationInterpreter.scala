package ubiqore.takibi.domain.projects

import cats._
import cats.data.EitherT
import cats.implicits._
import ubiqore.takibi.domain.{ProjectAlreadyExistsError, ProjectNotFoundError}

class ProjectValidationInterpreter[F[_]: Monad](repository: ProjectRepositoryAlgebra[F])
  extends ProjectValidationAlgebra[F] {

  def doesNotExist(project: Project): EitherT[F, ProjectAlreadyExistsError, Unit] = EitherT  {
    repository.findByNameAndFhirVersion(project.project_name, project.project_fhir_version).map{
       matches =>  if (matches.forall(p => p.project_base_iri != project.project_base_iri)){Right(())}
       else {Left(ProjectAlreadyExistsError(project))}
    }
  }

  def exists(projectId: Option[Long]): EitherT[F, ProjectNotFoundError.type, Unit] =
    EitherT {
      projectId match {
        case Some(id) =>
          // Ensure is a little tough to follow, it says "make sure this condition is true, otherwise throw the error specified
          // In this example, we make sure that the option returned has a value, otherwise the project.was not found
          repository.get(id).map {
            case Some(_) => Right(())
            case _ => Left(ProjectNotFoundError)
          }
        case _ =>
          Either.left[ProjectNotFoundError.type, Unit](ProjectNotFoundError).pure[F]
      }
    }
}

object ProjectValidationInterpreter {
  def apply[F[_]: Monad](repository: ProjectRepositoryAlgebra[F]) =
    new ProjectValidationInterpreter[F](repository)
}

