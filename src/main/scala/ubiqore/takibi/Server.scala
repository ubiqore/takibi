package ubiqore.takibi

import config._
import config.config._
import cats.effect._
import cats.implicits._
import doobie.util.ExecutionContexts
import io.circe.config.parser
import domain.projects.{ProjectService, ProjectValidationInterpreter}
import domain.valuesets.{ValueSetService, ValueSetValidationInterpreter}
import domain.profiles.{ProfileService,ProfileValidationInterpreter}
import infrastructure.repository.doobie._
import org.http4s.server.{Router, Server => H4Server}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import ubiqore.takibi.infrastructure.endpoint.ProjectEndpoints
import ubiqore.takibi.infrastructure.endpoint.ValueSetEndpoints
import ubiqore.takibi.infrastructure.endpoint.ProfileEndpoints

object Server extends IOApp {

   def createServer[F[_]: ContextShift : ConcurrentEffect : Timer] : Resource[F, H4Server[F]]  =
  /* def createServer[F[_]: ContextShift : ConcurrentEffect : Timer] : Resource[F,TakibiConfig]  = */

    for {
      conf              <- Resource.liftF(parser.decodePathF[F, TakibiConfig]("takibi"))
      connEc            <- ExecutionContexts.fixedThreadPool[F](conf.db.connections.poolSize)
      txnEc             <- ExecutionContexts.cachedThreadPool[F]
      xa                <- DatabaseConfig.dbTransactor(conf.db, connEc, txnEc)

      projectRepo        =  DoobieProjectRepositoryInterpreter[F](xa)
      projectValidation  =  ProjectValidationInterpreter[F](projectRepo)
      projectService     =  ProjectService[F](projectRepo, projectValidation)

      vsRepo        =  DoobieValueSetRepositoryInterpreter[F](xa)
      vsValidation  =  ValueSetValidationInterpreter[F](vsRepo)
      vsService     =  ValueSetService[F](vsRepo,vsValidation)

      prRepo        =  DoobieProfileRepositoryInterpreter[F](xa)
      prValidation  =  ProfileValidationInterpreter[F](prRepo)
      prService     =  ProfileService[F](prRepo,prValidation)

      services           =  ProjectEndpoints.endpoints[F](projectService)   <+> ValueSetEndpoints.endpoints[F](vsService) <+> ProfileEndpoints.endpoints[F](prService)
      httpApp            =  Router("/" -> services).orNotFound
      // _                 <- Resource.liftF(DatabaseConfig.initializeDb(conf.db))  Pour la gestion sql direecte.
      server            <-
        BlazeServerBuilder[F]
          .bindHttp(conf.server.port, conf.server.host)
          .withHttpApp(httpApp)
          .resource

    } yield server



  def run(args : List[String]) : IO[ExitCode] = {

    createServer.use{_ => IO.never }.as(ExitCode.Success)

  }
}
