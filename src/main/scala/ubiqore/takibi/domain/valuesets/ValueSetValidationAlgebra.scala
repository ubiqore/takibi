package ubiqore.takibi.domain.valuesets

import cats.data.EitherT

import ubiqore.takibi.domain.{ValueSetAlreadyExistsError, ValueSetNotFoundError, ValueSetWrongJsonError}

import scala.language.higherKinds

trait ValueSetValidationAlgebra[F[_]] {


  def doesNotExist(vs: ValueSet): EitherT[F, ValueSetAlreadyExistsError, Unit]
  def checkJson(vs: ValueSet): EitherT[F, ValueSetWrongJsonError, ValueSet]
  def exists(vsId: Option[Long]): EitherT[F, ValueSetNotFoundError.type, Unit]
}
