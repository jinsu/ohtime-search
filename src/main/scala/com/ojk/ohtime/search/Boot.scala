package com.ojk.ohtime.search

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.ojk.ohtime.search.services.RestRouting


object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("spray-can")

  // create and start our service actor
  val service = system.actorOf(Props(new RestRouting), "rest-routing")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8090 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8090)
}
