package com.mintbeans.rzmq

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.mintbeans.rzmq.ZMQMessages._
import com.typesafe.scalalogging.LazyLogging

object TaskPusherExample extends App with LazyLogging {

  implicit val system = ActorSystem("TaskPusherExample")
  implicit val materializer = ActorMaterializer()

  Source(Stream.from(1))
    //Simulate slow data generation
    .map(i => {
      Thread.sleep(1000)
      i
    })
    .map(i => s"Task to work on: ${i}")
    .map(s => ZMQMessage(ByteString(s)))
    .to(ZMQSink.pushSocket("tcp://127.0.0.1:5556"))
    .run()
}
