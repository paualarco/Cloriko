import io.grpc.ManagedChannelBuilder
import com.cloriko.protobuf.protocol.{MasterRequest, Updated, Update, MasterSlaveProtocolGrpc, HeartBeat, ProtocolGrpcMonix, JoinRequest, JoinReply, SlaveResponse}
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

val stub = ProtocolGrpcMonix.stub(channel)

val updated = Updated("updated01", "paualarco", "slave1")

val updateObservable = stub.updateStream(Observable.fromIterable[Updated](List(updated)))

val printConsumer: Consumer[Update, Unit] = {
  Consumer.foreach(println)
}



val a = Await.result(updateObservable.consumeWith(printConsumer).runAsync, 10 seconds)
