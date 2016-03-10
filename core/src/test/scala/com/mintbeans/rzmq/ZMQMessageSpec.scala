package com.mintbeans.rzmq

import akka.util.ByteString
import com.mintbeans.rzmq.ZMQMessages.ZMQMessage
import org.scalatest.{Matchers, WordSpec}

class ZMQMessageSpec extends WordSpec with Matchers {
  "ZMQMessage" should {
    "produce a zMsg with exactly 2 frames when given an explicit topic" in {
      val zMsg = ZMQMessage(ByteString("hello world")).zMsgWithTopic("test-topic")
      zMsg.size() should be (2)
    }
  }
}
