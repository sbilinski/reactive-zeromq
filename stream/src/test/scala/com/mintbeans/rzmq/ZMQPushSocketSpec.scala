package com.mintbeans.rzmq

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{ Sink, Source }
import akka.testkit.TestKit
import akka.util.ByteString
import com.mintbeans.rzmq.ZMQMessages._
import com.typesafe.scalalogging.LazyLogging
import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{ Seconds, Span }
import org.zeromq.{ ZContext, ZMQ, ZMsg }

import scala.collection.JavaConverters._

class ZMQPushSocketSpec extends TestKit(ActorSystem("ZMQPushSocketSpec"))
    with WordSpecLike
    with Matchers
    with GivenWhenThen
    with FakeZMQEndpoints
    with Eventually
    with BeforeAndAfterAll
    with LazyLogging {

  sealed trait TestContext {
    lazy val endpoint = "ipc://test-push-socket"
    lazy val pushSocket = Sink.fromGraph(new ZMQPushSocket(endpoint))
  }

  implicit val materializer = {
    val decider: Supervision.Decider = {
      case err =>
        logger.error("Unknown error. Stopping the stream.", err)
        Supervision.Stop
    }

    ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))(system)
  }

  implicit val patience = PatienceConfig(Span(10, Seconds))
  override val fakeContext = new ZContext(1)

  override def afterAll() = {
    system.terminate()
    fakeContext.close()
  }

  "ZMQPushSocket" should {
    "push messages to PULL sockets" in new TestContext {
      Given("a PULL socket is bound")
      zmqPullSession(endpoint) { pullSocket =>

        When("a fixed message is continuously delivered to the PUSH socket")
        Source(Stream.continually("hello world"))
          .map(s => ZMQMessage(ByteString(s)))
          .to(pushSocket)
          .run()

        Then("the PULL socket should receive a matching message")
        eventually {
          val msg = Option(ZMsg.recvMsg(pullSocket, ZMQ.DONTWAIT)).map { zMsg =>
            zMsg.asScala.toList.map(zFrame => ByteString(zFrame.getData))
          }.map {
            case payload :: Nil => new String(payload.toArray)
            case _ => fail("Invalid message format")
          }

          msg should be(Some("hello world"))
        }
      }
    }
  }

}
