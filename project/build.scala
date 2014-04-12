import sbt._
import sbt.Keys._
import Dependencies._
import xerial.sbt.Sonatype._
import SonatypeKeys._

object AppBuilder extends Build {
  
  val VERSION = "0.0.9-SNAPSHOT"
  val SCALA_VERSION = "2.10.3"
  val ORG = "com.github.scullxbones"

  def projectSettings(moduleName: String) = 
    Seq(name := "akka-persistence-mongo-"+moduleName, 
        organization := ORG,
        version := VERSION,
        scalaVersion := SCALA_VERSION,
        credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
        publishTo := Some("Sonatype Snapshots Nexus" at "http://192.168.0.105:8081/nexus/content/repositories/snapshots"),
	scalacOptions ++= Seq("-unchecked", "-deprecation","-feature"))
  
  val commonSettings = projectSettings("common")

  val casbahSettings = projectSettings("casbah")

  //val rxmongoSettings = projectSettings("rxmongo")
  
  //lazy val aRootNode = Project("root", file("."))
			//.settings (packagedArtifacts in file(".") := Map.empty)
//			.aggregate(common,casbah,rxmongo)
//			.aggregate(common,casbah)

  lazy val common = Project("common", file("common"))
    .settings(commonSettings : _*)
    .settings(libraryDependencies ++= commonDependencies)
    .settings(resolvers ++= projectResolvers)

  lazy val casbah = Project("casbah", file("casbah"))
    .settings(casbahSettings : _*)
    .settings(libraryDependencies ++= casbahDependencies)
    .settings(resolvers ++= projectResolvers)
    .dependsOn(common % "test->test;compile->compile")

//  lazy val rxmongo = Project("rxmongo", file("rxmongo"))
//    .settings(rxmongoSettings : _*)
//    .settings(libraryDependencies ++= rxmongoDependencies)
//    .settings(resolvers ++= projectResolvers)
//    .dependsOn(common % "test->test;compile->compile")


}
