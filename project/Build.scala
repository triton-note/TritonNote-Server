import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "TritonNote-Server"
  val appVersion      = "0.1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.amazonaws" % "aws-java-sdk" % "1.4.1",
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    "com.typesafe.slick" %% "slick" % "1.0.0",
    jdbc,
    anorm
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
