
import io.grpc.ManagedChannelBuilder
import com.cloriko.protobuf.protocol.{MasterRequest, MasterSlaveProtocolGrpc, HeartBeat, ProtocolGrpcMonix, JoinRequest, JoinReply, SlaveResponse}
import monix.eval.Task
import scala.concurrent.Await
import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Consumer
import monix.reactive.observables.ConnectableObservable
import monix.reactive.Observable

val channel = ManagedChannelBuilder
  .forAddress("localhost", 8980)
  .usePlaintext(true)
  .build()

val stub = ProtocolGrpcMonix.stub(channel) // only an async stub is provided

// Unary call
val joinReply: Task[JoinReply] = stub.join(JoinRequest("id1", "paualarco", "admin", "slaveId-1"))

val result: JoinReply = Await.result(joinReply.runAsync, 5 seconds)



val sealedValue = SlaveResponse.SealedValue.Heartbeat(HeartBeat(""))
val slaveResponse = SlaveResponse(sealedValue)
val obs = stub.protocol(Observable.fromIterable(List[SlaveResponse](slaveResponse)))


/*
val consumer: Consumer[HeartBeat, HeartBeat] = {
  Consumer.foldLeft(HeartBeat("0")) {
    (agg: HeartBeat, heartBeat: HeartBeat) =>
      HeartBeat(heartBeat.slaveId + "-" + agg.slaveId)
  }
}
*/

val printConsumer: Consumer[MasterRequest, Unit] = {
  Consumer.foreach(println)
}



val a = Await.result(obs.consumeWith(printConsumer).runAsync, 10 seconds)


