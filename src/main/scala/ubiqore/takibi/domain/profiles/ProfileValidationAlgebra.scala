package ubiqore.takibi.domain.profiles

import cats.data.EitherT
import ubiqore.takibi.domain.{ProfileAlreadyExistsError, ProfileNotFoundError, ProfileWrongJsonError}

import scala.language.higherKinds

trait ProfileValidationAlgebra[F[_]] {


  def doesNotExist(pr: Profile): EitherT[F, ProfileAlreadyExistsError, Unit]
  def checkJson(pr: Profile): EitherT[F, ProfileWrongJsonError, Profile]
  def exists(prId: Option[Long]): EitherT[F, ProfileNotFoundError.type, Unit]
}
