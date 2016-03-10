package com.mintbeans.rzmq

import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler }
import akka.stream.{ Attributes, Inlet, SinkShape }
import com.mintbeans.rzmq.ZMQErrors._
import com.mintbeans.rzmq.ZMQMessages._
import com.typesafe.scalalogging.LazyLogging
import org.zeromq.{ ZContext, ZMQ }

private[rzmq] class ZMQPushSocket(endpoint: String) extends GraphStage[SinkShape[ZMQMessage]] with LazyLogging {

  private val in: Inlet[ZMQMessage] = Inlet("ZMQPushSocket")

  override def shape: SinkShape[ZMQMessage] = SinkShape(in)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    logger.info("Initializing ZMQ context.")
    val context = new ZContext(1)
    logger.info(s"Binding PUSH socket to ${endpoint}")
    val socket = {
      val s = context.createSocket(ZMQ.PUSH)
      s.setSendTimeOut(0)
      s.connect(endpoint)
      s
    }

    setHandler(in, inHandler())

    override def preStart() = {
      pull(in)
    }

    override def postStop() = {
      logger.info("Closing socket.")
      context.destroySocket(socket)
      logger.info("Closing ZMQ context.")
      context.close()

      super.postStop()
    }

    def inHandler() = new InHandler {
      override def onPush(): Unit = {
        val message = grab(in)

        logger.debug(s"Sending message to ZMQ: ${message}")
        if (!message.zMsg().send(socket)) {
          failStage(new SendFailedException("Send failed"))
        }

        tryPull(in)
      }
    }
  }
}