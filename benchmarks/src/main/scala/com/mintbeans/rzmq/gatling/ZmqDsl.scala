package com.mintbeans.rzmq.gatling

import com.mintbeans.rzmq.gatling.process.{ SubscriberProcessBuilder, SubscriberProcessBuilderBase }
import com.mintbeans.rzmq.gatling.protocol.{ ZmqProtocol, ZmqProtocolBuilder, ZmqProtocolBuilderBase }
import io.gatling.core.action.builder.ActionBuilder

import scala.language.implicitConversions

trait ZmqDsl {

  val ZMQ = ZmqProtocolBuilderBase

  def subscriber(processName: String) = SubscriberProcessBuilderBase(processName)

  implicit def zmqProtocolBuilder2zmqProtocol(builder: ZmqProtocolBuilder): ZmqProtocol = builder.build
  implicit def subscriberProcessBuilder2ActionBuilder(builder: SubscriberProcessBuilder): ActionBuilder = builder.build()

}
