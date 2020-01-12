package com.cloriko.master

import monix.reactive.{ Consumer, OverflowStrategy }
import Master.initialDir
import monix.execution.CancelableFuture
import monix.reactive
import monix.reactive.observers.Subscriber
import monix.execution.Scheduler.Implicits.global
import monix.reactive.subjects.ConcurrentSubject
import monix.reactive.{ MulticastStrategy, Observable, Observer }

import scala.concurrent.duration._
import cats.implicits._
import cats.effect._
import monix.eval.Task
import com.cloriko.master.grpc.GrpcServer.GrpcChannel
import com.cloriko.protobuf.protocol._
import com.cloriko.common.Generators
import com.cloriko.common.DecoderImplicits._
import com.cloriko.common.logging.ImplicitLazyLogger

class Master(username: String) extends Generators with ImplicitLazyLogger {

  val masterSnapshot: Directory = initialDir
  var slaves = Map[String, SlaveRef]().empty //Map of (SlaveId -> (OperationId -> Operation))
  var numOfSlaves = slaves.size

  def registerSlave(slaveId: String): Task[Boolean] = {
    Task.eval {
      if (!slaves.contains(slaveId)) {
        logger.info(s"Master - Registering slave $slaveId to the slave quorum")
        slaves = slaves.updated(slaveId, SlaveRef(slaveId))
        true
      } else {
        logger.info(s"Master - The slave $slaveId was already part of the slave quorum")
        false
      }
    }
  }

  def consumerIdFilterSubject(fetchResponseSubscriber: ConcurrentSubject[SlaveResponse, SlaveResponse], id: String) = {
    Consumer.foreach[SlaveResponse] {
      logger.info(s"Master - Starting to consume filter with id: $id")
      slaveResponse =>
        logger.info(s"Master - Slave response received $slaveResponse ")
        logger.info(s"Master - DEBUG - Slave id ${slaveResponse.id} == id $id")
        if (slaveResponse.id == id) {
          logger.info(s"Master - Fetch response caught at master with id: $id")
          fetchResponseSubscriber.feed(List(slaveResponse))
          fetchResponseSubscriber.completedL.runAsync
        }
    }
  }
  def consumerIdFilter(fetchResponseSubscriber: Subscriber.Sync[SlaveResponse], id: String) = {
    Consumer.foreach[SlaveResponse] {
      logger.info(s"Master - Starting to consume filter with id: $id")
      slaveResponse =>
        logger.info(s"Master - Slave response received $slaveResponse ")
        logger.debug(s"Master - DEBUG - Slave id ${slaveResponse.id} == id $id")
        if (slaveResponse.id == id) {
          logger.info(s"Master - Fetch response caught at master with id: $id")
          fetchResponseSubscriber.onNextAll(List(slaveResponse))

        }
    }
  }
  def awaitFetchResponseConcurrentSubject(opId: String, slaveResponses: reactive.Observable[SlaveResponse]): CancelableFuture[SlaveResponse] = {
    logger.info(s"Master - Awaiting fetch response with id $opId")
    val subject: ConcurrentSubject[SlaveResponse, SlaveResponse] = ConcurrentSubject[SlaveResponse](MulticastStrategy.replay)
    val temporaryConsumer = slaveResponses.consumeWith(consumerIdFilterSubject(subject, opId)).runAsync

    //val future = fetchResponseObservable.firstL
    //temporaryConsumer.cancel()
    //future.runAsync
    subject.firstL.runAsync
  }

  def awaitFetchResponse(opId: String, slaveResponses: reactive.Observable[SlaveResponse]): CancelableFuture[SlaveResponse] = {
    logger.info(s"Master - Awaiting fetch response with id $opId")
    var temporaryConsumer: CancelableFuture[Unit] = Task.now().runAsync
    val fetchResponseObservable: Observable[SlaveResponse] = Observable.create(OverflowStrategy.Unbounded) {
      fetchResponseSubscriber: Subscriber.Sync[SlaveResponse] =>
        temporaryConsumer = slaveResponses.consumeWith(consumerIdFilter(fetchResponseSubscriber, opId)).runAsync
        temporaryConsumer
    }

    val future = fetchResponseObservable.firstL
    temporaryConsumer.cancel()
    future.runAsync

  }

  def sendRequest(request: MasterRequest): Option[CancelableFuture[SlaveResponse]] = {
    slaves.get(request.slaveId) match {
      case Some(slave: SlaveRef) => {
        slave.grpcChannel match {
          case Some(channel) => {
            logger.info(s"Master - Update operation sent to slave ${request.slaveId} of username: ${request.username}")
            slave.addPendingRequest(request.asProto)
            val fetchResponse = awaitFetchResponseConcurrentSubject(request.id, channel.downStream)
            channel.upStream.onNext(request.asProto)
            Some(fetchResponse)
          }
          case None => {
            logger.info(s"Master -  p of user ${request.username} not delivered since there was no update channel defined")
            None
          }
        }

      }
      case None => {
        logger.info(s"Master - Update op of user ${request.username} not delivered since slaveId ${request.slaveId} was not found")
        None
      }
    }
  }

  def registerChannel(slaveChannel: GrpcChannel[SlaveResponse, MasterRequest]): Task[Boolean] = {
    Task.eval {
      logger.info(s"Master - Received slave chanel at master from user: $username and slaveId: ${slaveChannel.slaveId}!")
      slaves.get(slaveChannel.slaveId) match {
        case Some(slave: SlaveRef) => {
          logger.info(s"Master - A new slave channel registered for slave ${slaveChannel.slaveId}")
          slave.grpcChannel = Some(slaveChannel)
          logger.info(s"Master - DEBUG - Strating to consume from ${slaveChannel.downStream} and producing using ${slaveChannel.upStream}")
          slaveChannel.downStream.consumeWith(simplePendingResponseConsumer)
          true
        }
        case None => logger.info(s"Slave ${slaveChannel.slaveId} not found"); false
      }
    }
  }

  val simplePendingResponseConsumer: Consumer.Sync[SlaveResponse, Unit] = {
    Consumer.foreach {
      updated =>
        {
          logger.info(s"Master - Consumed updated event $updated")
          slaves.get(updated.slaveId) match {
            case Some(slave) => slave.removePendingResponse(updated.id)

          }
        }
    }
  }
}

object Master {
  type OperationId = String
  val initialDir = Directory("root", "root", "/", Seq[Directory](), Seq[FileReference]())
  //todo in the future need to have a snapshot for each slave...
}

