package com.leysoft.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

object SimpleApi extends App {
  implicit val system = ActorSystem("simple-api")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val `host` = "localhost"
  val `port` = 8080

  val homeRoute: Route = path("index" / "home") {
    get {
      system.log.info("GET - home")
      complete {
        HttpEntity(ContentTypes.`application/json`, "Home")
      }
    }
  }

  /* Other
  val homeRoute: Route = (path("index" / "home") & get ){
    system.log.info("GET - home")
    complete {
      HttpEntity(ContentTypes.`application/json`, "Home")
    }
  }*/

  /**
   * QueryParams:
   * "param_name" = Symbol("param_name") -> String
   * "param_name".? -> Option[String] = Symbol("param_name").? -> Option[String]
   * "param_name" ? "defaultValue" = Symbol("param_name") ? "defaultValue"
   * "param_name".as[Int] = Symbol("param_name").as[Int]
   */
  val greetingRoute: Route = (path("greeting") & get & parameters("name".?)) {
    name =>
      system.log.info(s"GET - greeting $name")
      complete {
        name match {
          case Some(value) => HttpEntity(ContentTypes.`application/json`, s"Hello $value")
          case None => StatusCodes.BadRequest
        }
      }
  }

  val helloWorldRoute: Route = path("hello-world" / Segment) { name =>
    get {
      system.log.info(s"GET - hello-world $name")
      complete {
        HttpEntity(ContentTypes.`application/json`, s"Hello World $name")
      }
    }
  }

  val route: Route = concat(greetingRoute, homeRoute, helloWorldRoute)

  /*
  val route: Route = path("home") {
    concat(
      get {
        system.log.info("GET")
        complete {
          HttpEntity(ContentTypes.`application/json`, "Hello World")
        }
      },
      post{
        system.log.info("POST")
        complete {
          StatusCodes.OK
        }
      }
    )
  }*/

  /* Other
  val route: Route = path("home") {
    get {
      system.log.info("GET")
      complete(StatusCodes.OK)
    } ~
    post {
      system.log.info("POST")
      complete(StatusCodes.OK)
    }
  }*/

  Http().bindAndHandle(route, `host`, `port`)
}
