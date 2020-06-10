name := "akka-persistence-typed"

version := "0.1"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.6"
lazy val catsVersion = "2.1.1"
lazy val circeVersion = "0.13.0"
lazy val enumeratumVersion = "1.6.0"
lazy val refinedVersion = "0.9.13"
lazy val fUUIDVersion = "0.3.0"
lazy val logbackVersion = "1.2.3"
lazy val monixVersion = "3.1.0"
lazy val shapelessVersion = "2.3.3"
lazy val scalaLoggingVersion = "3.9.2"
lazy val sttpVersion = "2.1.1"
lazy val scalaTestVersion = "3.1.1"

val scalaCheckVersion = "1.14.3"

val cats = Seq(
"org.typelevel" %% "cats-core",
"org.typelevel" %% "cats-effect"
).map (_ % catsVersion )

val akka = Seq(
"com.typesafe.akka" %% "akka-actor-typed",
"com.typesafe.akka" %% "akka-serialization-jackson",
"com.typesafe.akka" %% "akka-persistence-typed",
"com.typesafe.akka" %% "akka-cluster-typed",
"com.typesafe.akka" %% "akka-stream-typed"
).map (_ % akkaVersion ) ++ Seq (
"com.typesafe.akka" %% "akka-actor-testkit-typed"
).map(_ % akkaVersion % Test)

val circe = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-refined"
).map(_ % circeVersion)

val enumeratum = Seq(
  "com.beachape" %% "enumeratum",
  "com.beachape" %% "enumeratum-circe"
).map(_ % enumeratumVersion)

val refined = Seq(
  "eu.timepit" %% "refined",
  "eu.timepit" %% "refined-cats",
  "eu.timepit" %% "refined-scalacheck",
  "eu.timepit" %% "refined-shapeless",
).map(_ % refinedVersion)

val sttp = Seq (
  "com.softwaremill.sttp.client"  %% "core",
  "com.softwaremill.sttp.client"  %% "async-http-client-backend-cats",
  "com.softwaremill.sttp.client"  %% "slf4j-backend",
  "com.softwaremill.sttp.client"  %% "circe"
).map(_ % sttpVersion)

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion ,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion,
  "org.typelevel" %% "cats-testkit" % catsVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion)
  .map(_ % Test)

libraryDependencies ++= cats ++ akka ++ circe ++ enumeratum ++ sttp ++ refined ++
  Seq(
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "io.monix" %% "monix" % monixVersion,
    "io.chrisdavenport" %% "fuuid" % fUUIDVersion,
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
    "org.iq80.leveldb" % "leveldb" % "0.12",
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion) ++
  testDependencies

scalacOptions in Compile ++= Seq(
  "-deprecation", // Warning and location for usages of deprecated APIs.
  "-encoding", "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature",  // For features that should be imported explicitly.
  "-unchecked",  // Generated code depends on assumptions.
  "-Xcheckinit",  // Wrap field accessors to throw an exception on uninitialized access.
  "-Xlint:adapted-args",  // An argument list is modified to match the receiver.
  "-Xlint:constant",  // Constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",  // Selecting member of DelayedInit.
  "-Xlint:doc-detached",  // A detached Scaladoc comment.
  "-Xlint:inaccessible",  // Inaccessible types in method signatures.
  "-Xlint:infer-any",  // A type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",  // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",  // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",  // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",  // Option.apply used implicit view.
  "-Xlint:package-object-classes",  // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",  // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",  // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",  // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
  //  "-Ypartial-unification",  // Enable partial unification in type constructor inference
  //  "-Ywarn-dead-code",  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",  // More than one implicit parameter section is defined.
  //  "-Ywarn-numeric-widen",  // Numerics are implicitly widened.
  "-Ywarn-unused:implicits",  // An implicit parameter is unused.
  "-Ywarn-unused:imports",   // An import selector is not referenced.
  "-Ywarn-unused:locals",   // A local definition is unused.
  //  "-Ywarn-unused:params",  // A value parameter is unused.
  "-Ywarn-unused:patvars",   // A variable bound in a pattern is unused.
  //  "-Ywarn-value-discard",   // Non-Unit expression results are unused.
  "-Ywarn-unused:privates"  // A private member is unused.
)
