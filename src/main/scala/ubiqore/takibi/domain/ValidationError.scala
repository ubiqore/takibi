package ubiqore.takibi.domain

import ubiqore.takibi.domain.profiles.Profile
import ubiqore.takibi.domain.projects.Project
import ubiqore.takibi.domain.valuesets.ValueSet


sealed trait ValidationError extends Product with Serializable

case class ProjectAlreadyExistsError(project: Project) extends ValidationError

case object ProjectNotFoundError extends ValidationError

sealed trait ValueSetValidationError extends Product with Serializable
case class   ValueSetAlreadyExistsError(vs: ValueSet) extends ValueSetValidationError
case class   ValueSetWrongJsonError(vs: ValueSet) extends ValueSetValidationError
case object  ValueSetNotFoundError extends ValueSetValidationError

sealed trait ProfileValidationError extends Product with Serializable
case class   ProfileAlreadyExistsError(profile: Profile) extends ProfileValidationError
case class   ProfileWrongJsonError(vs: Profile) extends ProfileValidationError
case object  ProfileNotFoundError extends ValidationError

//  case object OrderNotFoundError extends ValidationError
//case object UserNotFoundError extends ValidationError
//case class UserAlreadyExistsError(user: User) extends ValidationError
//case class UserAuthenticationFailedError(userName: String) extends ValidationError
