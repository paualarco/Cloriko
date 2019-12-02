package com.cloriko.slave

import com.cloriko.protobuf.protocol.HeartBeat
import monix.eval.Task
import monix.execution.Ack
import monix.reactive.observers.Subscriber

import scala.concurrent.duration._
object MonixFeeder {

      def asyncProduceEvents(sub: Subscriber[HeartBeat]): Task[Unit] = {
        Task.deferFuture(sub.onNext(HeartBeat("heey")))
          .delayExecution(100.millis)
          .flatMap {
            case Ack.Continue => asyncProduceEvents(sub)
            case Ack.Stop => Task.unit
          }
      }

}
