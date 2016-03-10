package com.mintbeans.rzmq

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.scalalogging.LazyLogging

object TaskPullerExample extends App with LazyLogging {

  implicit val system = ActorSystem("TaskPullerExample")
  implicit val materializer = ActorMaterializer()

  ZMQSource.pullSocket("tcp://*:5556")
    .map(msg => new String(msg.payload.toArray))
    .to(Sink.foreach(println))
    .run()
}
