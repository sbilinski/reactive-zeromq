package com.mintbeans.rzmq

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.LazyLogging

object NumberSubscriberExample extends App with LazyLogging {

  implicit val system = ActorSystem("NumberSubscriberExample")
  implicit val materializer = ActorMaterializer()

  ZMQSource.subSocket(endpoint = "tcp://127.0.0.1:5555", topic = "fancy_numbers")
    .map(msg => new String(msg.payload.toArray))
    .to(Sink.foreach(println))
    .run()
}
