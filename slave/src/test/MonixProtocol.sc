
import io.grpc.ManagedChannelBuilder
import com.cloriko.protobuf.protocol.{Updated, Update, ProtocolGrpcMonix, JoinRequest, JoinReply}
import monix.eval.Task
import scala.concurrent.Await
import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Consumer
import monix.reactive.Observable

val channel = ManagedChannelBuilder
  .forAddress("localhost", 8980)
  .usePlaintext(true)
  .build()


val stub = ProtocolGrpcMonix.stub(channel) // only an async stub is provided

// Unary call
val joinReply: Task[JoinReply] = stub.join(JoinRequest("id1", "paualarco", "admin", "slaveId-1"))

val result: JoinReply = Await.result(joinReply.runAsync, 5 seconds)



val updated = Updated("updated01", "paualarco", "slave1")

//val obs = stub.protocol(Observable.fromIterable(List[SlaveResponse](slaveResponse)))
//val obs = stub.updateStream(Observable.fromIterable(List[Updated](updated, updated, updated, updated, updated, updated)))//.delayOnNext(2 seconds))

val updateObservable = stub.updateStream(Observable.fromIterable[Updated](List(updated)))

/*
val consumer: Consumer[HeartBeat, HeartBeat] = {
  Consumer.foldLeft(HeartBeat("0")) {
    (agg: HeartBeat, heartBeat: HeartBeat) =>
      HeartBeat(heartBeat.slaveId + "-" + agg.slaveId)
  }
}
*/


val printConsumer: Consumer[Update, Unit] = {
  Consumer.foreach(println)
}

Await.result(updateObservable.consumeWith(printConsumer).runAsync, 10 seconds)



//val a = Await.result(obs.consumeWith(printConsumer).runAsync, 10 seconds)


