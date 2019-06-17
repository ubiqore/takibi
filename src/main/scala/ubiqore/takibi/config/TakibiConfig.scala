package ubiqore.takibi.config

final case class ServerConfig(host: String, port: Int)
final case class TakibiConfig(db: DatabaseConfig, server: ServerConfig)
