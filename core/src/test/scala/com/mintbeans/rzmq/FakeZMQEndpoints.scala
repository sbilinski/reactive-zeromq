package com.mintbeans.rzmq

import org.zeromq.{ ZMQ, ZContext }

private[rzmq] trait FakeZMQEndpoints {
  val fakeContext: ZContext

  def zmqPubSession(endpoint: String)(block: ZMQ.Socket => Unit) = {
    val socket = fakeContext.createSocket(ZMQ.PUB)
    socket.setSendTimeOut(0)
    socket.bind(endpoint)
    session(socket)(block)
  }

  def zmqSubSession(endpoint: String, topic: String)(block: ZMQ.Socket => Unit) = {
    val socket = fakeContext.createSocket(ZMQ.SUB)
    socket.subscribe(topic.getBytes(ZMQ.CHARSET))
    socket.connect(endpoint)
    session(socket)(block)
  }

  private def session(socket: ZMQ.Socket)(block: ZMQ.Socket => Unit): Unit = {
    try {
      block(socket)
    } finally {
      socket.close()
    }
  }
}
