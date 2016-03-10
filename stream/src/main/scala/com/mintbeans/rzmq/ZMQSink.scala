package com.mintbeans.rzmq

import akka.NotUsed
import akka.stream.scaladsl.Sink
import com.mintbeans.rzmq.ZMQMessages._

object ZMQSink {
  def pubSocket(endpoint: String, topic: String): Sink[ZMQMessage, NotUsed] =
    Sink.fromGraph(new ZMQPubSocket(endpoint, topic))

  def pushSocket(endpoint: String): Sink[ZMQMessage, NotUsed] =
    Sink.fromGraph(new ZMQPushSocket(endpoint))
}