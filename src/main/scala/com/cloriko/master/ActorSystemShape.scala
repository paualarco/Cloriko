package com.cloriko.master

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext

trait ActorSystemShape {
  implicit val system: ActorSystem
  implicit val executionContext: ExecutionContext

}
