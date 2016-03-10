package com.mintbeans.rzmq

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import akka.testkit.{ TestKit, TestProbe }
import akka.util.ByteString
import org.scalatest.{ GivenWhenThen, BeforeAndAfterAll, Matchers, WordSpecLike }
import org.zeromq.ZContext

import scala.concurrent.duration._
import scala.language.postfixOps

class ZMQSubSocketSpec extends TestKit(ActorSystem("ZMQSubSocketSpec"))
    with WordSpecLike
    with Matchers
    with GivenWhenThen
    with FakeZMQEndpoints
    with BeforeAndAfterAll {

  sealed trait TestContext {
    lazy val endpoint = "ipc://test-sub-socket"
    lazy val topic = "some-sub-topic"
    lazy val subSocket = Source.fromGraph(new ZMQSubSocket(endpoint, topic))
  }

  implicit val materializer = ActorMaterializer()
  override val fakeContext = new ZContext(1)

  override def afterAll() = {
    system.terminate()
    fakeContext.close()
  }

  "ZMQSubSocket" should {
    "subscribe to messages published by PUB sockets" in new TestContext {
      Given("a PUB socket")
      zmqPubSession(endpoint) { pubSocket =>

        And("an attached message handler")
        val handler = TestProbe()
        subSocket
          .map(m => new String(m.payload.toArray))
          .to(Sink.actorRef(handler.ref, "completed"))
          .run()

        When("a message is sent to the PUB socket")
        val msg1 = new ZMQMessage(ByteString("hello world"))
        val sent = msg1.zMsgWithTopic(topic).send(pubSocket)
        sent should be(true)

        Then("the handler should receive a matching message")
        handler.expectMsg(10 seconds, "hello world")
      }
    }
  }
}
