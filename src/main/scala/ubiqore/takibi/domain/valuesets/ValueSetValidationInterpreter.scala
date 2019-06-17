package ubiqore.takibi.domain.valuesets

import cats._
import cats.data.EitherT
import cats.implicits._

import ubiqore.takibi.domain.{ValueSetAlreadyExistsError, ValueSetNotFoundError, ValueSetWrongJsonError}


import scala.util.Try
class ValueSetValidationInterpreter[F[_]: Monad](repository: ValueSetRepositoryAlgebra[F])
  extends ValueSetValidationAlgebra[F] {

  def doesNotExist(valueSet: ValueSet): EitherT[F, ValueSetAlreadyExistsError, Unit] = EitherT  {
    repository.findByIdentityAndMaturity(valueSet.valueset_identity,valueSet.valueset_maturity).map{
       one =>  if (one.isDefined)
                    Left(ValueSetAlreadyExistsError(valueSet))
               else {Right(())}
    }

  }

  def checkJson(vs: ValueSet): EitherT[F, ValueSetWrongJsonError , ValueSet] = EitherT {
    val test1 = vs.valueset_content.isObject
    val test2 = Try{vs.valueset_content.hcursor.get[String]("resourceType").right.get.equals("ValueSet")}.toOption

    val test3 = test2.getOrElse(false)

    test1 && test3    match { case false =>
                  Either.left[ValueSetWrongJsonError, ValueSet](ValueSetWrongJsonError(vs)).pure[F]
                case  true =>
                  Either.right[ValueSetWrongJsonError, ValueSet](vs).pure[F]
    }

    }


  def exists(valueSetId: Option[Long]): EitherT[F, ValueSetNotFoundError.type, Unit] =
    EitherT {
      valueSetId match {
        case Some(id) =>
          // Ensure is a little tough to follow, it says "make sure this condition is true, otherwise throw the error specified
          // In this example, we make sure that the option returned has a value, otherwise the project.was not found
          repository.read(id).map {
            case Some(_) => Right(())
            case _ => Left(ValueSetNotFoundError)
          }
        case _ =>
          Either.left[ValueSetNotFoundError.type, Unit](ValueSetNotFoundError).pure[F]
      }
    }
}

object ValueSetValidationInterpreter {
  def apply[F[_]: Monad](repository: ValueSetRepositoryAlgebra[F]) =
    new ValueSetValidationInterpreter[F](repository)
}


