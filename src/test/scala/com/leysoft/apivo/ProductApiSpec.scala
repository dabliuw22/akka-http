package com.leysoft.apivo

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.leysoft.Specification

final class ProductApiSpec extends Specification with ScalatestRouteTest {

  "ProductApi" should {
    "Return All Products" in {
      Get("/products") ~> ProductRoute.route ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        responseAs[String] shouldEqual
          """[{"name":"producto1","price":10.0,"provider":{"name":"provider1"}},{"name":"producto2","price":10.0,"provider":{"name":"provider2"}}]""".stripMargin
      }
    }
  }
}
