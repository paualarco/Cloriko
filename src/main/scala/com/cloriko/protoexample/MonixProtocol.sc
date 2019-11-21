
import io.grpc.ManagedChannelBuilder
import com.cloriko.protobuf.protocol.{HeartBeat, JoinRequest, JoinReply, ProtocolGrpcMonix}
import monix.eval.Task
import scala.concurrent.Await
import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Consumer

val channel = ManagedChannelBuilder
  .forAddress("localhost", 8980)
  .usePlaintext(true)
  .build()

val stub = ProtocolGrpcMonix.stub(channel) // only an async stub is provided

// Unary call
val joinReply: Task[JoinReply] = stub.join(JoinRequest("1", "Pau", "replica1"))

val result: JoinReply = Await.result(joinReply.runAsync, 2 seconds)

println(s"Reply: $result")


val obs = stub.heartbeat(JoinReply("1"))

val consumer: Consumer[HeartBeat, HeartBeat] = {
  Consumer.foldLeft(HeartBeat("0")) {
    (agg: HeartBeat, heartBeat: HeartBeat) =>
      HeartBeat(heartBeat.replicaId + "-" + agg.replicaId)
  }
}
val res = Await.result(obs.consumeWith(consumer).runAsync, 3 seconds)


