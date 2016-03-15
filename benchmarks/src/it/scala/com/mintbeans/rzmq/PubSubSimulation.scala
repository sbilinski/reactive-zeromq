package com.mintbeans.rzmq

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.mintbeans.rzmq.ZMQMessages.ZMQMessage
import com.mintbeans.rzmq.gatling.Predef._
import io.gatling.core.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps

class PubSubSimulation extends Simulation {
  val endpoint = "ipc://pub-sub-simulation"
  val topic = "some-topic"

  implicit val system = ActorSystem("PubSubSimulation")
  implicit val materializer = ActorMaterializer()

  Source(Stream.from(1))
    .map(i => ZMQMessage(ByteString(i)))
    .to(ZMQSink.pubSocket(endpoint, topic))
    .run()

  val zmqConfig = ZMQ.endpoint(endpoint).receiveTimeout(100 millis)
  val subscribers = scenario("Consume Stream").exec(
    subscriber("simple-subscriber").topic(topic).messagesToRead(5)
  )

  setUp(
    subscribers.inject(
      atOnceUsers(4),
      nothingFor(1 second),
      constantUsersPerSec(5) during (30 seconds)
    )
  ).protocols(zmqConfig).maxDuration(1 minute)
}
