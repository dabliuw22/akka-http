package com.leysoft

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object SimpleServer extends App {
  implicit val system = ActorSystem("simple-server-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val serverSource = Http().bind(interface = "localhost", port = 8080)
  val connectionSink = Sink.foreach[IncomingConnection] { connection =>
    println(s"Incoming connection $connection")
  }
  serverSource.to(connectionSink).run().onComplete {
    case Success(value) => println(s"Success $value")
    case Failure(exception) => println(s"Failure $exception")
  }

  // Sync serve HTTP responses
  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/sync"), _, _, _) =>
      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<html>
          | <body>
          |   Hello from Akka HTTP!
          | </body>
          |</html>
        """.stripMargin))
    case request: HttpRequest => request.discardEntityBytes()
      HttpResponse(StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<html>
          | <body>
          |   OOPS! The resource can't be found.
          | </body>
          |</html>
        """.stripMargin))
  }

  val httpSyncConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
    connection.handleWithSyncHandler(requestHandler)
  }

  Http().bind(interface = "localhost", port = 8081).runWith(httpSyncConnectionHandler)
  //Http().bindAndHandleSync(requestHandler, "localhost", 8080)

  // Async serve HTTP responses
  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/async"), _, _, _) => Future(HttpResponse(StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            | <body>
            |   Hello from Akka HTTP!
            | </body>
            |</html>
          """.stripMargin)))
    case request: HttpRequest => request.discardEntityBytes()
      Future(HttpResponse(StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<html>
          | <body>
          |   OOPS! The resource can't be found.
          | </body>
          |</html>
        """.stripMargin)))
  }

  val httpAsyncConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
    connection.handleWithAsyncHandler(asyncRequestHandler)
  }

  Http().bind(interface = "localhost", port = 8082).runWith(httpAsyncConnectionHandler)

  // Async via Akka streams
  val streamsBasedRequestHandler: Flow[HttpRequest, HttpResponse, _] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET, uri @ Uri.Path("/async"), _, _, _) =>
      val queryParams = uri.query()
      if (!queryParams.isEmpty) queryParams.foreach(param => println(param))
      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<html>
          | <body>
          |   Hello from Akka HTTP!
          | </body>
          |</html>
        """.stripMargin))
    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<html>
          | <body>
          |   OOPS! The resource can't be found.
          | </body>
          |</html>
        """.stripMargin))
  }

  Http().bindAndHandle(streamsBasedRequestHandler, "localhost", 8083)
}
