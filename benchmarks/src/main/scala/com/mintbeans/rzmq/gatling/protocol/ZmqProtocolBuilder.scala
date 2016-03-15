package com.mintbeans.rzmq.gatling.protocol

import scala.concurrent.duration._

object ZmqProtocolBuilderBase {
  def endpoint(address: String) = ZmqProtocolBuilder(address)
}

case class ZmqProtocolBuilder(endpoint: String, receiveTimeout: FiniteDuration = 500 millis) {

  def receiveTimeout(duration: FiniteDuration) = copy(receiveTimeout = duration)

  def build = ZmqProtocol(
    endpoint = endpoint,
    receiveTimeout = receiveTimeout
  )
}