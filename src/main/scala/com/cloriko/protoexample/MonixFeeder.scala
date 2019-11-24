package com.cloriko.protoexample

import monix.eval.Task
import monix.reactive.observers.Subscriber
import monix.execution.Ack

import concurrent.duration._
/*
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
*/ 