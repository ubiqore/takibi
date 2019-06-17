package ubiqore.takibi.domain.profiles


import scala.language.higherKinds
import cats._
import cats.data._
import ubiqore.takibi.domain.{ ProfileNotFoundError, ProfileValidationError}


/**
  * The entry point to our domain, works with repositories and validations to implement behavior
  * @param repository where we get our data
  * @param validation something that provides validations to the service
  * @tparam F - this is the container for the things we work with, could be scala.concurrent.Future, Option, anything
  *           as long as it is a Monad
  */
class ProfileService[F[_]](repository: ProfileRepositoryAlgebra[F], validation: ProfileValidationAlgebra[F]) {
  import cats.syntax.all._

  def create(pr: Profile)(implicit M: Monad[F]): EitherT[F, ProfileValidationError, Profile] = for {
    _ <- validation.doesNotExist(pr)
    ok <- validation.checkJson(pr)
    saved <- EitherT.liftF(repository.create(ok))
  } yield saved

  /* Could argue that we could make this idempotent on put and not check if the project exists */
  def update(pr: Profile)(implicit M: Monad[F]): EitherT[F, ProfileNotFoundError.type, Profile] = for {
    _ <- validation.exists(pr.profile_id)
    saved <- EitherT.fromOptionF(repository.update(pr), ProfileNotFoundError)
  } yield saved

  def read(id: Long)(implicit M: Monad[F]): EitherT[F, ProfileNotFoundError.type, Profile] =
    EitherT.fromOptionF(repository.read(id), ProfileNotFoundError)


  def delete(id: Long)(implicit M: Monad[F]): F[Unit] =
    repository.delete(id).as(())


}

object ProfileService {
  def apply[F[_]: Monad](repository: ProfileRepositoryAlgebra[F], validation: ProfileValidationAlgebra[F]) =
    new ProfileService[F](repository, validation)
}

