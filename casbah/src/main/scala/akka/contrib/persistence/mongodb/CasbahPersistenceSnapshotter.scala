package akka.contrib.persistence.mongodb

import akka.persistence.{SelectedSnapshot, SnapshotMetadata}
import akka.persistence.serialization.Snapshot
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import scala.concurrent._
import akka.actor.ExtendedActorSystem
import scala.language.implicitConversions
import akka.actor.ActorSystem
import akka.serialization.Serialization

object CasbahPersistenceSnapshotter {
   import SnapshottingFieldNames._
 
  implicit def serializeSnapshot(snapshot: SelectedSnapshot)(implicit serialization: Serialization): DBObject =
	    MongoDBObject(PROCESSOR_ID -> snapshot.metadata.processorId,
	      SEQUENCE_NUMBER -> snapshot.metadata.sequenceNr,
	      TIMESTAMP -> snapshot.metadata.timestamp,
	      SERIALIZED -> serialization.serializerFor(classOf[Snapshot]).toBinary(Snapshot(snapshot.snapshot)))
	      
  implicit def deserializeSnapshot(document: DBObject)(implicit serialization: Serialization): SelectedSnapshot = {
     val processorId = document.as[String](PROCESSOR_ID)
     val sequenceNr = document.as[Long](SEQUENCE_NUMBER)
     val timestamp = document.as[Long](TIMESTAMP)
    val content = document.as[Array[Byte]](SERIALIZED)
     SelectedSnapshot(SnapshotMetadata(processorId, sequenceNr, timestamp), serialization.deserialize(content, classOf[Snapshot]).get.data)
   }
  
}

class CasbahPersistenceSnapshotter(driver: CasbahPersistenceDriver) extends MongoPersistenceSnapshottingApi {
  
  import CasbahPersistenceSnapshotter._
  import SnapshottingFieldNames._
  
  private[this] implicit val serialization = driver.serialization
  private[this] lazy val writeConcern = driver.snapsWriteConcern
  
  private[this] def snapQueryMaxSequenceMaxTime(pid: String, maxSeq: Long, maxTs: Long) = 
  	$and(PROCESSOR_ID $eq pid, SEQUENCE_NUMBER $lte maxSeq, TIMESTAMP $lte maxTs)
  
  private[mongodb] def findYoungestSnapshotByMaxSequence(pid: String, maxSeq: Long, maxTs: Long)(implicit ec: ExecutionContext) = Future {
    driver.breaker.withSyncCircuitBreaker {
      snaps.find(snapQueryMaxSequenceMaxTime(pid, maxSeq, maxTs))
        .sort(MongoDBObject(SEQUENCE_NUMBER -> -1, TIMESTAMP -> -1))
        .limit(1)
        .collectFirst {
          case o: DBObject => deserializeSnapshot(o)
        }
    }
  }

  private[mongodb] def saveSnapshot(snapshot: SelectedSnapshot)(implicit ec: ExecutionContext) = Future {
    driver.breaker.withSyncCircuitBreaker { snaps.insert(snapshot, writeConcern) }
  }
  
  private[mongodb] def deleteSnapshot(pid: String, seq: Long, ts: Long)(implicit ec: ExecutionContext) = driver.breaker.withSyncCircuitBreaker {
    snaps.remove($and(PROCESSOR_ID $eq pid, SEQUENCE_NUMBER $eq seq, TIMESTAMP $eq ts),writeConcern)
  }

  private[mongodb] def deleteMatchingSnapshots(pid: String, maxSeq: Long, maxTs: Long)(implicit ec: ExecutionContext) =
    driver.breaker.withSyncCircuitBreaker {
      snaps.remove(snapQueryMaxSequenceMaxTime(pid, maxSeq, maxTs), writeConcern)
    }
  
  
  private[mongodb] def snaps(implicit ec: ExecutionContext): MongoCollection = {
    val snapsCollection = driver.collection(driver.snapsCollectionName)
    snapsCollection.ensureIndex(MongoDBObject(PROCESSOR_ID -> 1, SEQUENCE_NUMBER -> -1, TIMESTAMP -> -1),
      MongoDBObject("unique" -> true, "name" -> driver.snapsIndexName))
    snapsCollection
  }
}