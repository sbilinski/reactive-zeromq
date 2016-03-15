package com.mintbeans.rzmq.gatling.process

import com.mintbeans.rzmq.zmq.action.SubscriberActionBuilder
import io.gatling.core.action.builder.ActionBuilder

case class SubscriberProcessBuilderBase(processName: String) {
  def topic(topic: String) = SubscriberProcessBuilder(processName, topic)
}

case class SubscriberProcessBuilder(processName: String, topic: String, messagesToRead: Int = 1) {
  def messagesToRead(numberOfMessages: Int) = {
    require(numberOfMessages > 0, "Number of messages to read must be non-negative.")
    copy(messagesToRead = numberOfMessages)
  }

  def build(): ActionBuilder = SubscriberActionBuilder(processName, topic, messagesToRead)
}