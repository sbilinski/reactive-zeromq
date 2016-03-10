import com.typesafe.sbt.SbtScalariform._

//
// Commons
//
lazy val commonSettings = Seq(
  organization  := "com.mintbeans",
  version       := "0.1",
  startYear     := Some(2016),
  scalaVersion  := "2.11.8",
  updateOptions := updateOptions.value.withCachedResolution(true),
  scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8"),
  resolvers     ++= Seq(
    "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Sonatype Releases"   at "http://oss.sonatype.org/content/repositories/releases",
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  ),
  libraryDependencies ++= {
    val configVersion       = "1.3.0"
    val scalaLoggingVersion = "3.1.0"
    val akkaVersion         = "2.4.2"
    val jeromqVersion       = "0.3.5"
    val logbackVersion      = "1.1.5"
    val scalaMockVersion    = "3.2.1"
    Seq(
      "com.typesafe"               %   "config"                      % configVersion,
      "com.typesafe.scala-logging" %%  "scala-logging"               % scalaLoggingVersion,
      "com.typesafe.akka"          %%  "akka-actor"                  % akkaVersion,
      "com.typesafe.akka"          %%  "akka-stream"                 % akkaVersion,
      "com.typesafe.akka"          %%  "akka-testkit"                % akkaVersion % "test",
      "com.typesafe.akka"          %%  "akka-stream-testkit"         % akkaVersion % "test",
      "org.zeromq"                 %   "jeromq"                      % jeromqVersion,
      "ch.qos.logback"             %   "logback-classic"             % logbackVersion % "test",
      "org.scalamock"              %%  "scalamock-scalatest-support" % scalaMockVersion % "test",
      //Required for IntelliJ ScalaTest integration
      "org.scala-lang.modules"     %%  "scala-xml"                   % "1.0.1" % "test"
    )
  }
)

// 
// Projects
//
lazy val stream   = project.in(file("stream"))
                           .settings(commonSettings: _*)
                           .settings(SbtScalariform.scalariformSettings: _*)

lazy val examples = project.in(file("examples"))
                           .settings(commonSettings: _*)
                           .settings(SbtScalariform.scalariformSettings: _*)
                           .settings(
                             libraryDependencies ++= {
                               val logbackVersion      = "1.1.5"
                               Seq(
                                 "ch.qos.logback" % "logback-classic" % logbackVersion
                               )
                             }
                           )
                           .dependsOn(stream)

lazy val root     = project.in(file(".")).aggregate(stream, examples)

