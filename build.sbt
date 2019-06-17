

lazy val CirceVersion           = "0.11.1"
lazy val CirceConfigVersion     = "0.6.1"
lazy val classy = "0.4.0"
lazy val kantan = "0.5.0"
lazy val FlywayVersion   = "5.2.4"
lazy val DoobieVersion = "0.6.0"
lazy val CatsVersion            = "1.6.0"
lazy val EnumeratumCirceVersion = "1.5.21"
lazy val Http4sVersion          = "0.20.0-M7"
lazy val Http4sRhoVersion       = "0.19.0-M6"
lazy val LogbackVersion         = "1.2.3"
lazy val ScalaCheckVersion      = "1.14.0"
lazy val ScalaTestVersion       = "3.0.7"
lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.5.11"
lazy val hapiFhirVersion    ="3.7.0"
lazy val BetterFileversion = "3.7.2-SNAPSHOT"

resolvers += Resolver.sonatypeRepo("snapshots")

/*
 */
scalaVersion    := "2.12.8"
    
    libraryDependencies ++= Seq(

    "com.github.pathikrit" %% "better-files" % BetterFileversion,
      "org.typelevel"         %% "cats-core"              % CatsVersion,
      // posgres
      // Start with this one
      "org.tpolecat" %% "doobie-core"      % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % DoobieVersion, // Postgres driver 42.2.5 + type mappings.
      "org.tpolecat" %% "doobie-hikari"  % DoobieVersion,
      "org.typelevel"         %% "cats-core"              % CatsVersion,
      
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "ca.uhn.hapi.fhir"  % "hapi-fhir-base"                     % hapiFhirVersion ,
      "ca.uhn.hapi.fhir"  % "hapi-fhir-structures-r4"            % hapiFhirVersion ,
      "ca.uhn.hapi.fhir"  % "hapi-fhir-validation"               % hapiFhirVersion ,
      "ca.uhn.hapi.fhir"  % "hapi-fhir-validation-resources-r4"  % hapiFhirVersion ,

      // json parser based on Cat
      "io.circe"              %% "circe-core"                    % CirceVersion,
      "io.circe"              %% "circe-generic"                 % CirceVersion,
      "io.circe"              %% "circe-parser"                  % CirceVersion,
      "io.circe"              %% "circe-generic-extras"   % CirceVersion,
      "io.circe"              %% "circe-parser"           % CirceVersion,
      "io.circe"              %% "circe-java8"            % CirceVersion,
      "io.circe"              %% "circe-config"           % CirceConfigVersion,
      "com.beachape"          %% "enumeratum-circe"       % EnumeratumCirceVersion,

      "com.47deg" %% "classy-core"            % classy ,
      "com.47deg" %% "classy-config-typesafe" % classy ,
      "com.47deg" %% "classy-generic"         % classy ,
      "com.47deg" %% "classy-cats"            % classy,

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,


      "com.nrinaudo"      %% "kantan.csv"             % kantan,
      "com.nrinaudo"      %% "kantan.csv-java8"       % kantan,
      "com.nrinaudo"      %% "kantan.csv-cats"        % kantan,
      "com.nrinaudo"      %% "kantan.csv-generic"     % kantan,
      "com.nrinaudo"      %% "kantan.csv-joda-time"   % kantan,
      "com.nrinaudo"      %% "kantan.csv-refined"     % kantan,
      "com.nrinaudo"      %% "kantan.csv-enumeratum"  % kantan,
      "com.nrinaudo"      %% "kantan.csv-libra"       % kantan,

      "org.http4s"            %% "http4s-blaze-server"    % Http4sVersion,
      "org.http4s"            %% "http4s-circe"           % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"             % Http4sVersion,
      "org.http4s"            %% "rho-swagger"            % Http4sRhoVersion,
      "org.http4s"            %% "http4s-blaze-client"    % Http4sVersion  ,
      "ch.qos.logback"        %  "logback-classic"        % LogbackVersion,
      "org.scalacheck"        %% "scalacheck"             % ScalaCheckVersion % Test,
      "org.scalatest"         %% "scalatest"              % ScalaTestVersion  % Test,
      "org.flywaydb"          %  "flyway-core"        % FlywayVersion ,
       
        "org.webjars" % "webjars-locator" % "0.34",
        "org.webjars" % "swagger-ui"      % "3.17.3"
      

  )

//
//scalacOptions ++= Seq(
//  "-feature",
//  "-deprecation",
//  "-unchecked",
//  "-language:postfixOps",
//  "-language:higherKinds",
//  "-Ypartial-unification")


scalacOptions ++= Seq(
  // format: off
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xfuture",                          // Turn on future language features.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification",             // Enable partial unification in type constructor inference
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
 // "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
  // format: on
)

// Filter out compiler flags to make the repl experience functional...
val badConsoleFlags = Seq("-Xfatal-warnings", "-Ywarn-unused:imports")
scalacOptions in (Compile, console) ~= (_.filterNot(badConsoleFlags.contains(_)))

enablePlugins(ScalafmtPlugin, JavaAppPackaging, GhpagesPlugin, MicrositesPlugin, TutPlugin)


// Note: This fixes error with sbt run not loading config properly
fork in run := true