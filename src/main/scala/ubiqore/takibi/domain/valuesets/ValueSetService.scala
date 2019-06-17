package ubiqore.takibi.domain.valuesets





import scala.language.higherKinds
import cats._
import cats.data._
import ubiqore.takibi.domain.{ ValueSetNotFoundError, ValueSetValidationError}


/**
  * The entry point to our domain, works with repositories and validations to implement behavior
  * @param repository where we get our data
  * @param validation something that provides validations to the service
  * @tparam F - this is the container for the things we work with, could be scala.concurrent.Future, Option, anything
  *           as long as it is a Monad
  */
class ValueSetService[F[_]](repository: ValueSetRepositoryAlgebra[F], validation: ValueSetValidationAlgebra[F]) {
  import cats.syntax.all._

  def create(vs: ValueSet)(implicit M: Monad[F]): EitherT[F, ValueSetValidationError, ValueSet] = for {
    _ <- validation.doesNotExist(vs)
    ok <- validation.checkJson(vs)
    saved <- EitherT.liftF(repository.create(ok))
  } yield saved

  /* Could argue that we could make this idempotent on put and not check if the project exists */
  def update(vs: ValueSet)(implicit M: Monad[F]): EitherT[F, ValueSetNotFoundError.type, ValueSet] = for {
    _ <- validation.exists(vs.valueset_id)
    saved <- EitherT.fromOptionF(repository.update(vs), ValueSetNotFoundError)
  } yield saved

  def read(id: Long)(implicit M: Monad[F]): EitherT[F, ValueSetNotFoundError.type, ValueSet] =
    EitherT.fromOptionF(repository.read(id), ValueSetNotFoundError)


  def delete(id: Long)(implicit M: Monad[F]): F[Unit] =
    repository.delete(id).as(())


}

object ValueSetService {
  def apply[F[_]: Monad](repository: ValueSetRepositoryAlgebra[F], validation: ValueSetValidationAlgebra[F]) =
    new ValueSetService[F](repository, validation)
}

