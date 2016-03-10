package com.mintbeans.rzmq

import scala.util.control.NoStackTrace

object ZMQErrors {

  sealed class ZMQException(msg: String) extends RuntimeException

  final class SendFailedException(msg: String) extends ZMQException(msg) with NoStackTrace
  final class ReadFailedException(msg: String) extends ZMQException(msg) with NoStackTrace
  final class MessageFormatException(msg: String) extends ZMQException(msg) with NoStackTrace

}
