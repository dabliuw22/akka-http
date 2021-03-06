package com.leysoft.apivo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object ProductApi extends App {
  import akka.http.scaladsl.server.Directives._
  implicit val system = ActorSystem("user-system")
  implicit val materialize = ActorMaterializer()
  val host = "localhost"
  val port = 8080

  val routes = concat(ProductRoute.route)

  Http().bindAndHandle(routes, host, port)
}
