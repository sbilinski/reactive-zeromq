package com.mintbeans.rzmq.gatling.action

import akka.actor.{ ActorRef, Props }
import akka.util.ByteString
import com.mintbeans.rzmq.gatling.protocol.ZmqProtocol
import io.gatling.core.action.{ Failable, Interruptable }
import io.gatling.core.result.message.{ ResponseTimings, Status }
import io.gatling.core.result.writer.StatsEngine
import io.gatling.core.session.Session
import io.gatling.core.validation._
import org.zeromq.{ ZContext, ZMQ, ZMsg }

import scala.collection.JavaConverters._

object SubscriberAction {
  def props(processName: String, protocol: ZmqProtocol, topic: String, messagesToRead: Int, statsEngine: StatsEngine, next: ActorRef): Props =
    Props(new SubscriberAction(processName, protocol, topic, messagesToRead, statsEngine, next))
}

class SubscriberAction(
    processName: String,
    protocol: ZmqProtocol,
    topic: String,
    messagesToRead: Int,
    val statsEngine: StatsEngine,
    val next: ActorRef
) extends Interruptable with Failable {

  val expectedTopicBytes = ByteString(topic)
  val mainCtx = new ZContext(1)

  override def postStop() = {
    mainCtx.close()
  }

  override def executeOrFail(session: Session): Validation[String] = {
    val ctx = ZContext.shadow(mainCtx)
    try {
      statsEngine.logRequest(session, processName)

      val requestStartDate = now()
      val socket = ctx.createSocket(ZMQ.SUB)
      socket.setReceiveTimeOut(protocol.receiveTimeout.toMillis.toInt)
      socket.subscribe(topic.getBytes(ZMQ.CHARSET))
      socket.connect(protocol.endpoint)
      val requestEndDate = now()

      val messageStream = {
        val serverStream = for (i <- Stream.range(0, messagesToRead)) yield readMessage(socket)
        serverStream.takeWhile(validContent _).map(extractPayload _)
      }

      val responseStartDate = now()
      val messages = messageStream.toList
      val responseEndDate = now()

      val timings = ResponseTimings(requestStartDate, requestEndDate, responseStartDate, responseEndDate)

      messages match {
        case list if list.size == messagesToRead =>
          statsEngine.logResponse(session, processName, timings, Status("OK"), None, None)
          Success("OK")
        case list =>
          statsEngine.logResponse(session, processName, timings, Status("KO"), None, None)
          Failure(s"Invalid list size ${list.size}")
      }
    } finally {
      ctx.close()
      session.terminate()
    }
  }

  @inline
  private def validContent(response: Option[Either[String, ByteString]]): Boolean = response match {
    case Some(Right(_)) => true
    case _ => false
  }

  @inline
  private def extractPayload(response: Option[Either[String, ByteString]]): ByteString = response match {
    case Some(Right(bytes)) => bytes
    case _ => throw new IllegalStateException("Extracting payload from an invalid response.")
  }

  @inline
  private def readMessage(socket: ZMQ.Socket) = {
    Option(ZMsg.recvMsg(socket)).map { zMsg =>
      zMsg.asScala.toList.map(zFrame => ByteString(zFrame.getData))
    }.map {
      case topic :: payload :: Nil if topic == expectedTopicBytes => Right(payload)
      case topic :: payload :: Nil => Left(s"Invalid topic: ${new String(topic.toArray, ZMQ.CHARSET)}")
      case _ => Left("Invalid message format")
    }
  }

  @inline
  private def now() = System.currentTimeMillis()

}
