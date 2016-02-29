# Reactive Streams for ZeroMQ

A minimal [Akka Streams](http://doc.akka.io/docs/akka-stream-and-http-experimental/current/scala.html) library for 
implementing flows with [ZeroMQ](http://zeromq.org) sockets, inspired by [Reactive Kafka](https://github.com/softwaremill/reactive-kafka). Features:

* `PUB` socket sink
* `SUB` socket source

Internal communication is currently implemented using [JeroMQ](https://github.com/zeromq/jeromq) (a pure Java implementation of ZMQ).