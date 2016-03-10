package com.mintbeans.rzmq

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import akka.testkit.{ TestKit, TestProbe }
import akka.util.ByteString
import com.mintbeans.rzmq.ZMQMessages._
import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{ Seconds, Span }
import org.zeromq.ZContext

import scala.concurrent.duration._
import scala.language.postfixOps

class ZMQPullSocketSpec extends TestKit(ActorSystem("ZMQPullSocketSpec"))
    with WordSpecLike
    with Matchers
    with GivenWhenThen
    with FakeZMQEndpoints
    with Eventually
    with BeforeAndAfterAll {

  sealed trait TestContext {
    lazy val endpoint = "ipc://test-pull-socket"
    lazy val pullSocket = Source.fromGraph(new ZMQPullSocket(endpoint))
  }

  implicit val materializer = ActorMaterializer()
  implicit val patience = PatienceConfig(Span(10, Seconds))
  override val fakeContext = new ZContext(1)

  override def afterAll() = {
    system.terminate()
    fakeContext.close()
  }

  "ZMQPullSocket" should {
    "process messages published by PUSH sockets" in new TestContext {
      Given("a PUSH socket")
      zmqPushSession(endpoint) { pushSocket =>

        And("an attached message handler")
        val handler = TestProbe()
        pullSocket
          .map(m => new String(m.payload.toArray))
          .to(Sink.actorRef(handler.ref, "completed"))
          .run()

        When("a message is sent to the PUSH socket")
        val msg1 = new ZMQMessage(ByteString("hello world"))
        val sent = msg1.zMsg().send(pushSocket)
        sent should be(true)

        Then("the handler should receive a matching message")
        handler.expectMsg(10 seconds, "hello world")
      }
    }
  }

}
