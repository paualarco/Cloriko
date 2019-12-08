package com.cloriko.slave

import com.cloriko.protobuf.protocol.{JoinReply, JoinRequest, ProtocolGrpcMonix, Update, Updated}
import io.grpc.ManagedChannelBuilder
import monix.eval.Task
import monix.execution.Cancelable
import monix.execution.Scheduler.Implicits.global
import monix.reactive.observers.Subscriber
import monix.reactive.{Consumer, Observable, OverflowStrategy}

class Slave(username: String) {
  val slaveId = "randomSlaveId"
  var updateChannel: Option[SlaveUpdateChannel] = None

  val channel = ManagedChannelBuilder
    .forAddress("localhost", 8980)
    .usePlaintext(true)
    .build()

  val stub = ProtocolGrpcMonix.stub(channel) // only an async stub is provided

  def joinRequestCloriko(password: String): Task[Boolean] = {
    println(s"Slave - Received join request at slave: $slaveId of user: $username")
    val joinReply: Task[JoinReply] = stub.join(JoinRequest("id1", username, password, slaveId))
    joinReply.map { reply =>
      if(reply.authenticated) {
        println(s"Slave - JoinRequest accepted for user!")
        initUpdateFlow(username, slaveId)
        //todo check that all flows were correctly initialized
        true
      }
      else {
        println("Slave - JoinRequest rejected")
        false
      }
    }
  }

  val updateCousumer: Subscriber.Sync[Updated] => Consumer.Sync[Update, Unit] = { updateUpstream =>
    println("Slave - Starting update consumer")
    Consumer.foreach[Update]{ update =>
      println(s"Slave - Update received: $update, returning Updated event, updatedUpstream used: $updateUpstream")
      updateUpstream.onNext(Updated(update.id, username, slaveId)) }
  }

  val updateCousumerFromObserver: (Observable[Update], Subscriber.Sync[Updated]) => Cancelable = { (observable, updateUpstream) =>
    println("Slave - Starting update consumer")
    val consumer = Consumer.foreach[Update]{ update =>
      println(s"Slave - Update received: $update, returning Updated event, updatedUpstream used: $updateUpstream")
      updateUpstream.onNext(Updated(update.id, username, slaveId)) }
    observable.consumeWith(consumer).runAsync
  }

  val printConsumer: Consumer[Update, Unit] = {
    Consumer.foreach(u => println(s"Slave - Consumed updates $u"))
  }

  def initUpdateFlow(username: String, slaveId: String) = {
    println(s"Slave - Initializing update flow from slave $slaveId to $username cluster")
    val updated = Updated("0", username, slaveId)
    //val updateDownstream = stub.updateStream(Observable.fromIterable[Updated](List(updated)))

    val ob = stub.updateStream{
      val obs = Observable.create[Updated](OverflowStrategy.Unbounded) { updateUpstream: Subscriber.Sync[Updated] =>
        println(s"Slave - UpdatedUpStream created: $updateUpstream and actioned")
        updateChannel = Some(SlaveUpdateChannel(username, slaveId, Some(updateUpstream), None))
        updateUpstream.onNext(updated)
        //updateCousumer(updateUpstream)
        Task.now(println("Slave - Running dummy task")).runAsync
      }
      //obs.runAsyncGetFirst
      println(s"Slave - UpdateStream called, observable created: $obs")
    obs
    }
    updateChannel = Some(SlaveUpdateChannel(username, slaveId, None, Some(ob)))

    //ob.consumeWith(updateCousumer(updateChannel.get.updateUpstream.get)).runAsync

    val consumerStarter: Task[Update] = ob.consumeWith(Consumer.head[Update])
    println("Checkpoint print")
     consumerStarter.runAsync.onSuccess {
       case _: Update => {
         val currentUpdatedUpstream = updateChannel.get.updateUpstream.get
         println(s"Slave - Starting to consume update events from updateUpstream ${updateChannel.get.updateUpstream.get}")
         ob.consumeWith(updateCousumer(currentUpdatedUpstream)).runAsync
       }
       case _ => println("Slave - Update consumer not started... SOmething failed")
     }


    //    Await.result(obs.consumeWith(printConsumer).runAsync, 10 seconds)

  }
  case class SlaveUpdateChannel(username: String, slaveId: String, updateUpstream: Option[Subscriber.Sync[Updated]], updateDownstream: Option[Observable[Update]])

}

object Slave {
  def apply(username: String): Slave = new Slave(username)
}
