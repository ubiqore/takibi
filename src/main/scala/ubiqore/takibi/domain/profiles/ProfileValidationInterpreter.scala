package ubiqore.takibi.domain.profiles

import cats._
import cats.data.EitherT
import cats.implicits._
import ubiqore.takibi.domain.{ProfileAlreadyExistsError, ProfileNotFoundError, ProfileWrongJsonError}

import scala.util.Try

class ProfileValidationInterpreter[F[_]: Monad](repository: ProfileRepositoryAlgebra[F])
  extends ProfileValidationAlgebra[F] {

  def doesNotExist(pr: Profile): EitherT[F, ProfileAlreadyExistsError, Unit] = EitherT  {
    repository.findByIdentityAndMaturity(pr.profile_identity,pr.profile_maturity).map{
       one =>  if (one.isDefined)
                    Left(ProfileAlreadyExistsError(pr))
               else {Right(())}
    }

  }

  def checkJson(pr: Profile): EitherT[F, ProfileWrongJsonError , Profile] = EitherT {
    val test1 = pr.profile_content.isObject
    val test2 = Try{pr.profile_content.hcursor.get[String]("resourceType").right.get.equals("StructureDefinition")}.toOption

    val test3 = test2.getOrElse(false)

    test1 && test3    match { case false =>
                  Either.left[ProfileWrongJsonError, Profile](ProfileWrongJsonError(pr)).pure[F]
                case  true =>
                  Either.right[ProfileWrongJsonError, Profile](pr).pure[F]
    }

    }


  def exists(profileId: Option[Long]): EitherT[F, ProfileNotFoundError.type, Unit] =
    EitherT {
      profileId match {
        case Some(id) =>
          // Ensure is a little tough to follow, it says "make sure this condition is true, otherwise throw the error specified
          // In this example, we make sure that the option returned has a value, otherwise the project.was not found
          repository.read(id).map {
            case Some(_) => Right(())
            case _ => Left(ProfileNotFoundError)
          }
        case _ =>
          Either.left[ProfileNotFoundError.type, Unit](ProfileNotFoundError).pure[F]
      }
    }
}

object ProfileValidationInterpreter {
  def apply[F[_]: Monad](repository: ProfileRepositoryAlgebra[F]) =
    new ProfileValidationInterpreter[F](repository)
}





