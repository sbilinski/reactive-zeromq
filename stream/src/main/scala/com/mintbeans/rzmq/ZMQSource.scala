package com.mintbeans.rzmq

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.mintbeans.rzmq.ZMQMessages._

object ZMQSource {
  def subSocket(endpoint: String, topic: String): Source[ZMQMessage, NotUsed] =
    Source.fromGraph(new ZMQSubSocket(endpoint, topic))

  def pullSocket(endpoint: String): Source[ZMQMessage, NotUsed] =
    Source.fromGraph(new ZMQPullSocket(endpoint))
}