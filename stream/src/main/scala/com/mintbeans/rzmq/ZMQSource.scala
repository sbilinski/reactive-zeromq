package com.mintbeans.rzmq

import akka.NotUsed
import akka.stream.scaladsl.Source

object ZMQSource {
  def subSocket(endpoint: String, topic: String): Source[ZMQMessage, NotUsed] =
    Source.fromGraph(new ZMQSubSocket(endpoint, topic))
}