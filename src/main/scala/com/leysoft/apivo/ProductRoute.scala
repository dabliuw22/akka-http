package com.leysoft.apivo

import akka.http.scaladsl.server.Route

object ProductRoute {
  import akka.http.scaladsl.server.Directives._
  import ProductJsonFormat._

  val prod = productFormat

  private val products = List(
    Product("producto1", 10.00, Provider("provider1")),
    Product("producto2", 10.00, Provider("provider2"))
  )

  private val prefix = "products"

  def route: Route = pathPrefix(prefix) {
    concat(getAll)
  }

  private def getAll: Route = get {
    complete(products)
  }
}
