import sbt._

object Dependencies {

    val coinport_repo = "coinport-repo" at "http://192.168.0.105:8081/nexus/content/groups/public"
    val typesafeReleases = "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
    val typesafeSnapshots = "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
    val local = "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
    val akkap_tck = "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

    val projectResolvers = Seq(
        coinport_repo,
        typesafeReleases,
        typesafeSnapshots,
        Resolver.sonatypeRepo("snapshots"),
        akkap_tck
        
        )

  val scalatest = "org.scalatest" %% "scalatest" % "2.0" % "test"
  val junit = "junit" % "junit" % "4.11" % "test"
  val scalaMock = "org.scalamock" %% "scalamock-scalatest-support" % "3.1.RC1" % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
  val embedMongoScalatest = "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.1" % "test"
  val embedMongo = "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.42" % "test"
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % "2.3.0" % "test"
  val akkaPersistTck = "com.github.krasserm" %% "akka-persistence-testkit" % "0.3" % "test"

  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.0"
  val rxMongo = "org.reactivemongo" %% "reactivemongo" % "0.10.0"
  val casbah = "org.mongodb" %% "casbah" % "2.6.4"
  val metrix = "nl.grons" %% "metrics-scala" % "3.0.5_a2.3"
 
  val testDependencies = Seq(
    scalatest,
    junit,
    mockito,
    akkaTestKit,
    akkaPersistTck
  )
 
  val commonDependencies = Seq(
    akkaPersistence,
    metrix
  ) ++ testDependencies

  val casbahDependencies = Seq(
    casbah,
    embedMongo 
  ) ++ commonDependencies

  val rxmongoDependencies = Seq(
    rxMongo,
    embedMongo 
  ) ++ commonDependencies

}
