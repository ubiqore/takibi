package ubiqore.takibi.domain.projects

import enumeratum.{Enum, EnumEntry,CirceEnum}


sealed trait ProjectStatus extends EnumEntry

case object ProjectStatus extends Enum[ProjectStatus] with CirceEnum[ProjectStatus] {

  case object Started extends ProjectStatus
  case object ValidationPending extends ProjectStatus
  case object Validated extends ProjectStatus
  case object Cancelled extends ProjectStatus
  case object Fixed extends ProjectStatus

  val values = findValues
}
