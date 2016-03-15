package com.mintbeans.rzmq.zmq.action

import akka.actor.{ ActorSystem, ActorRef }
import com.mintbeans.rzmq.gatling.action.SubscriberAction
import com.mintbeans.rzmq.gatling.protocol.ZmqProtocol
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.{ Protocols, GatlingConfiguration }
import io.gatling.core.structure.ScenarioContext

case class SubscriberActionBuilder(processName: String, topic: String, messagesToRead: Int) extends ActionBuilder {

  /**
   * This is likely to change with a Gatling upgrade. Consider the JMS implementation as a reference:
   *
   * https://github.com/gatling/gatling/blob/master/gatling-jms/src/main/scala/io/gatling/jms/action/JmsReqReplyActionBuilder.scala
   */
  override def build(system: ActorSystem, next: ActorRef, ctx: ScenarioContext): ActorRef = {
    val protocol = ctx.protocols.protocol[ZmqProtocol]
    val statsEngine = ctx.statsEngine

    system.actorOf(SubscriberAction.props(processName, protocol, topic, messagesToRead, statsEngine, next), actorName("subscriber"))
  }

}
