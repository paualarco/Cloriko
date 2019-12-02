import io.grpc.ManagedChannelBuilder
import monix.eval.Task
import scala.concurrent.Await
import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import monix.reactive.Consumer

val obi = Observable.fromIterable(1 to 4)

val consumer: Consumer[Int, Int] = Consumer.foldLeft(0)(_ + _)

val result = Await.result(obi.consumeWith(consumer).runAsync, 1 seconds)

obs