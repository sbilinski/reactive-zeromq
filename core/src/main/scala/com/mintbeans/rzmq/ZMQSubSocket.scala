package com.mintbeans.rzmq

import akka.stream.stage.{ GraphStage, GraphStageLogic, OutHandler }
import akka.stream.{ Attributes, Outlet, SourceShape }
import akka.util.ByteString
import com.mintbeans.rzmq.ZMQSubSocket.{ MessageFormatException, ReadFailedException }
import com.typesafe.scalalogging.LazyLogging
import org.zeromq.{ ZContext, ZMQ, ZMsg }

import scala.collection.JavaConverters._
import scala.util.control.NoStackTrace

private[rzmq] object ZMQSubSocket {
  final class MessageFormatException(msg: String) extends ZMQException(msg) with NoStackTrace
  final class ReadFailedException(msg: String) extends ZMQException(msg) with NoStackTrace
}

private[rzmq] class ZMQSubSocket(endpoint: String, topic: String) extends GraphStage[SourceShape[ZMQMessage]] with LazyLogging {

  val out: Outlet[ZMQMessage] = Outlet("ZMQSourceGraphStage")
  override val shape: SourceShape[ZMQMessage] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    logger.info("Initializing ZMQ context.")
    val context = new ZContext(1)
    logger.info(s"Connecting SUB socket to ${endpoint}")
    val socket = {
      val s = context.createSocket(ZMQ.SUB)
      s.subscribe(topic.getBytes(ZMQ.CHARSET))
      s.connect(endpoint)
      s
    }

    setHandler(out, outHandler())

    override def postStop() = {
      logger.info("Closing socket.")
      context.destroySocket(socket)
      logger.info("Closing ZMQ context.")
      context.close()

      super.postStop()
    }

    def outHandler() = new OutHandler {
      override def onPull(): Unit = {
        logger.debug("Reading message...")
        Option(ZMsg.recvMsg(socket)).map { zMsg =>
          zMsg.asScala.toList.map(zFrame => ByteString(zFrame.getData))
        }.map {
          case topic :: payload :: Nil => {
            logger.debug(s"Received topic (${topic}) message: ${payload}")
            push(out, ZMQMessage(payload))
          }
          case _ => fail(out, new MessageFormatException("Invalid message format"))
        }
      }

      override def onDownstreamFinish(): Unit = {
        logger.info("Downstream finish.")
        super.onDownstreamFinish()
      }
    }

  }
}
