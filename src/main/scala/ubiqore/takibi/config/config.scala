package ubiqore.takibi.config

import io.circe.Decoder
import io.circe.generic.semiauto._

package object config {
  implicit val serverDec: Decoder[ServerConfig] = deriveDecoder
  implicit val dbconnDec: Decoder[DatabaseConnectionsConfig] = deriveDecoder
  implicit val dbDec: Decoder[DatabaseConfig] = deriveDecoder
  implicit val takibiDec: Decoder[TakibiConfig] = deriveDecoder
}
