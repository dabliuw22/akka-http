package com.leysoft.apivo

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object ProductJsonFormat extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val providerFormat = jsonFormat1(Provider)

  implicit val productFormat = jsonFormat3(Product)
}
