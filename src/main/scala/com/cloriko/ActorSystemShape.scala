package com.cloriko

import akka.actor.ActorSystem
import com.cloriko.QuickstartServer.system

import scala.concurrent.ExecutionContext

trait ActorSystemShape {
  implicit val system: ActorSystem
  implicit val executionContext: ExecutionContext

}
