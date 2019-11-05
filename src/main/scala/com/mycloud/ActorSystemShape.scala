package com.mycloud

import akka.actor.ActorSystem
import com.mycloud.QuickstartServer.system

import scala.concurrent.ExecutionContext

trait ActorSystemShape {
  implicit val system: ActorSystem
  implicit val executionContext: ExecutionContext

}
