package akka.contrib.persistence.mongodb

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite
import com.mongodb.casbah.MongoConnection
import de.flapdoodle.embed.mongo.distribution._
import de.flapdoodle.embed.mongo._
import de.flapdoodle.embed.process.io.directories._
import de.flapdoodle.embed.process.extract._
import de.flapdoodle.embed.mongo._
import de.flapdoodle.embed.process.config.IRuntimeConfig
import de.flapdoodle.embed.mongo.config._
import de.flapdoodle.embed.process.runtime.Network

trait EmbedMongo extends BeforeAndAfterAll { this: BeforeAndAfterAll with Suite =>
  def embedConnectionURL: String = { "localhost" }
  lazy val embedConnectionPort: Int = { Network.getFreeServerPort() }
  def embedDB: String = { "test" }

  val artifactStorePath = new PlatformTempDir()
  val executableNaming = new UUIDTempNaming()
  val command = Command.MongoD
  val runtimeConfig: IRuntimeConfig  = new RuntimeConfigBuilder()
    .defaults(command)
    .artifactStore(new ArtifactStoreBuilder()
        .defaults(command)
        .download(new DownloadConfigBuilder()
            .defaultsForCommand(command)
            .artifactStorePath(artifactStorePath))
        .executableNaming(executableNaming))
        .build();
  
  val mongodConfig = new MongodConfigBuilder()
    .version(Version.Main.PRODUCTION)
    .cmdOptions(new MongoCmdOptionsBuilder()
    	.syncDeplay(1)
    	.build())
    .net(new Net("127.0.0.1",embedConnectionPort, Network.localhostIsIPv6()))
    .build();
  
  lazy val runtime = MongodStarter.getInstance(runtimeConfig);
  lazy val mongod = runtime.prepare(mongodConfig);
  lazy val mongodExe = mongod.start()

  override def beforeAll() {
    mongodExe
    val col = mongoDB("system.profile")
    col.findOne
    super.beforeAll()
  }

  override def afterAll() {
    super.afterAll()
    mongod.stop(); mongodExe.stop()
  }

  lazy val mongoDB = MongoConnection(embedConnectionURL, embedConnectionPort)(embedDB)
}