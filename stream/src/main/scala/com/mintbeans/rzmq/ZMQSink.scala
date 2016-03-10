package com.mintbeans.rzmq

import akka.NotUsed
import akka.stream.scaladsl.Sink

object ZMQSink {
  def pubSocket(endpoint: String, topic: String): Sink[ZMQMessage, NotUsed] =
    Sink.fromGraph(new ZMQPubSocket(endpoint, topic))
}