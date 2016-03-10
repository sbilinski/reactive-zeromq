package com.mintbeans.rzmq

import akka.util.ByteString
import org.zeromq.{ ZMQ, ZMsg }

final case class ZMQMessage(payload: ByteString) {
  private[rzmq] def zMsgWithTopic(topic: String) = {
    val zmsg = new ZMsg
    zmsg.add(topic.getBytes(ZMQ.CHARSET))
    zmsg.add(payload.toArray)
    zmsg
  }
}