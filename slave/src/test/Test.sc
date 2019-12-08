import io.grpc.ManagedChannelBuilder
import com.cloriko.protobuf.protocol.{MasterSlaveProtocolGrpc, HeartBeat, ProtocolGrpcMonix, JoinRequest, JoinReply, SlaveResponse, MasterRequest}


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
val joinReply: Task[JoinReply] = stub.join(JoinRequest("1", "Pau", "replica1"))


val result: JoinReply = Await.result(joinReply.runAsync, 2 seconds)


val sealedValue = SlaveResponse.SealedValue.Heartbeat(HeartBeat(""))
val slaveResponse = SlaveResponse(sealedValue)
val obs = stub.protocol(Observable.fromIterable(List[SlaveResponse](slaveResponse)))

val printConsumer: Consumer[MasterRequest, Unit] = {
  Consumer.foreach(println)
}

val res = Await.result(obs.consumeWith(printConsumer).runAsync, 10 seconds)



