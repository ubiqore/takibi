package ubiqore.takibi.config


import cats.effect.{Async, ContextShift, Resource }  // Sync for flyaway

import doobie.hikari.HikariTransactor
//import org.flywaydb.core.Flyway
//import cats.syntax.functor._
import scala.concurrent.ExecutionContext

case class DatabaseConnectionsConfig(poolSize: Int)
case class DatabaseConfig(url: String, driver: String, user: String, password: String, connections: DatabaseConnectionsConfig)

object DatabaseConfig {
  def dbTransactor[F[_]: Async : ContextShift](
                                                dbc: DatabaseConfig,
                                                connEc : ExecutionContext,
                                                transEc : ExecutionContext
                                              ): Resource[F, HikariTransactor[F]] =
    HikariTransactor.newHikariTransactor[F](dbc.driver, dbc.url, dbc.user, dbc.password, connEc, transEc)

  /**
    * Runs the flyway migrations against the target database

  def initializeDb[F[_]](cfg : DatabaseConfig)(implicit S: Sync[F]): F[Unit] =
    S.delay {
      val fw: Flyway = {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
      }
      fw.migrate()
    }.as(())
    */
  }
