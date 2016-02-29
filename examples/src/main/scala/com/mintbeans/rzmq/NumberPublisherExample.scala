package com.mintbeans.rzmq

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging

object NumberPublisherExample extends App with LazyLogging {

  implicit val system = ActorSystem("NumberPublisherExample")
  implicit val materializer = ActorMaterializer()

  Source(Stream.from(1))
    //Simulate slow data generation
    .map(i => {
      Thread.sleep(1000)
      i
    })
    .map(i => s"Fancy number: ${i}")
    .map(s => ZMQMessage(ByteString(s)))
    .to(ZMQSink.pubSocket(endpoint = "tcp://127.0.0.1:5555", topic = "fancy_numbers"))
    .run()
}
