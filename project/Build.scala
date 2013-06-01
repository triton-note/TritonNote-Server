import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "TritonNote-Server"
  val appVersion      = "0.1.0-SNAPSHOT"

  val appDependencies = Seq(
    "net.databinder.dispatch" %% "dispatch-core" % "0.10.1",
    "securesocial" %% "securesocial" % "master-SNAPSHOT",
    "com.amazonaws" % "aws-java-sdk" % "1.4.5",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "com.typesafe.slick" %% "slick" % "1.0.0",
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq(
      Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),
      Resolver.url("sbt-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
    )
  )

}
