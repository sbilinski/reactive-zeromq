package com.mintbeans.rzmq

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Source, Sink }
import akka.testkit.TestKit
import akka.util.ByteString
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{ Seconds, Span }
import org.scalatest._
import org.zeromq.{ ZMQ, ZContext, ZMsg }

import scala.collection.JavaConverters._

class ZMQPubSocketSpec extends TestKit(ActorSystem("ZMQPubSocketSpec"))
    with WordSpecLike
    with Matchers
    with GivenWhenThen
    with FakeZMQEndpoints
    with Eventually
    with BeforeAndAfterAll {

  sealed trait TestContext {
    lazy val endpoint = "ipc://test-pub-socket"
    lazy val topic = "some-pub-topic"
    lazy val pubSocket = Sink.fromGraph(new ZMQPubSocket(endpoint, topic))
  }

  implicit val materializer = ActorMaterializer()
  implicit val patience = PatienceConfig(Span(10, Seconds))
  override val fakeContext = new ZContext(1)

  override def afterAll() = {
    system.terminate()
    fakeContext.close()
  }

  "ZMQPubSocket" should {
    "publish messages for SUB sockets" in new TestContext {
      Given("a fixed message is continuously delivered to the PUB socket")
      Source(Stream.continually("hello world"))
        .map(s => ZMQMessage(ByteString(s)))
        .to(pubSocket)
        .run()

      When("a SUB socket connects")
      zmqSubSession(endpoint, topic) { subSocket =>

        Then("it should recive a matching message")
        eventually {
          val msg = Option(ZMsg.recvMsg(subSocket, ZMQ.DONTWAIT)).map { zMsg =>
            zMsg.asScala.toList.map(zFrame => ByteString(zFrame.getData))
          }.map {
            case topic :: payload :: Nil => new String(payload.toArray)
            case _ => fail("Invalid message format")
          }

          msg should be(Some("hello world"))
        }

      }
    }
  }

}
