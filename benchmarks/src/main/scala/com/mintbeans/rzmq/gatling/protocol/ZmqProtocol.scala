package com.mintbeans.rzmq.gatling.protocol

import io.gatling.core.config.Protocol

import scala.concurrent.duration.FiniteDuration

case class ZmqProtocol(endpoint: String, receiveTimeout: FiniteDuration) extends Protocol
