package com.leysoft.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.javadsl.server.{MalformedQueryParamRejection, MalformedRequestContentRejection}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, MethodRejection, MissingQueryParamRejection, RejectionHandler, Route}
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol
import spray.json._

import scala.collection.mutable.Map
import scala.collection.immutable.List
import scala.concurrent.{ExecutionContext, Future}

object ExampleApi extends App {
  implicit val exceptionHandler = ErrorHandler.exceptionHandler
  implicit val rejectionHandler = ErrorHandler.rejectionHandler
  implicit val system = ActorSystem("example-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val `host` = "localhost"
  val `port` = 8080

  val authorRouter = AuthorRouter(AuthorRepository(), system.log).route

  val route = concat(authorRouter)

  Http().bindAndHandle(route, `host`, `port`)
}

final case class Author(id: Int, name: String)

final case class Error(code: Int, reason: String, message: String)

trait AuthorJsonProtocol extends DefaultJsonProtocol {

  implicit val format = jsonFormat2(Author)
}

trait ErrorJsonProtocol extends DefaultJsonProtocol {

  implicit val errorFormat = jsonFormat3(Error)
}

case class AuthorRouter(authorRepository: AuthorRepository, logger: LoggingAdapter)
                       (implicit executionContext: ExecutionContext)
  extends SprayJsonSupport with AuthorJsonProtocol {

  def route: Route = pathPrefix("authors") {
    pathEnd {
      concat(getById, getAll, create, update)
    }
  }

  private def getById: Route = (get & parameters("id".as[Int])) { id =>
    logger.info(s"GET - id: $id")
    val author = authorRepository.findById(id)
    logger.info(s"Author: ${author.toJson.prettyPrint}")
    author match {
      case Some(value) => complete(value)
      case  _ => throw new NullPointerException(s"Not Found Author: $id")
    }
  }

  private def getAll: Route = get {
    logger.info("GET - All")
    val futureAuthors = authorRepository.findAll
    complete(futureAuthors)
  }

  private def create: Route = (post & entity(as[Author])) { author =>
    logger.info(s"POST - Author: ${author.toJson.prettyPrint}")
    val savedAuthor = authorRepository.save(author)
    complete(savedAuthor)
  }

  private def update: Route = (put & entity(as[Author])) { author =>
    logger.info(s"PUT - Author: ${author.toJson.prettyPrint}")
    val savedAuthor = authorRepository.save(author)
    complete(savedAuthor)
  }
}

case class AuthorRepository() (implicit executionContext: ExecutionContext) {

  val authors = Map[Int, Author](1 -> Author(1, "Author1"),
    2 -> Author(2, "Author2"),
    3 -> Author(3, "Author3"))

  def save(author: Author): Future[Author] =
    Future(authors.put(author.id, author)).map { _ => author }

  def findById(id: Int): Option[Author] = authors.get(id)

  def findAll: Future[List[Author]] = Future(List.from(authors.values))
}

object ErrorHandler extends SprayJsonSupport with ErrorJsonProtocol {

  /*
  val rejectionHandler: RejectionHandler = _ => {
    Some(complete((BadRequest, Error(BadRequest.intValue,
      BadRequest.defaultMessage, BadRequest.reason))))
  }*/

  def rejectionHandler = RejectionHandler.newBuilder()
    .handle {
      case _: MalformedRequestContentRejection => complete((BadRequest,
        Error(BadRequest.intValue, BadRequest.defaultMessage, BadRequest.reason)))
    }.handle {
      case _: MalformedQueryParamRejection => complete((BadRequest,
        Error(BadRequest.intValue, BadRequest.defaultMessage, BadRequest.reason)))
      case _: MissingQueryParamRejection => complete((BadRequest,
        Error(BadRequest.intValue, BadRequest.defaultMessage, BadRequest.reason)))
    }.handleAll[MethodRejection] { _ =>
      complete(HttpResponse(MethodNotAllowed, entity = HttpEntity(ContentTypes.`application/json`,
        Error(MethodNotAllowed.intValue, MethodNotAllowed.defaultMessage,
          MethodNotAllowed.reason).toJson.prettyPrint)))
    }.handleNotFound { complete((BadRequest,
      Error(BadRequest.intValue, BadRequest.defaultMessage, BadRequest.reason)))
    }.result

  def exceptionHandler = ExceptionHandler {
    case e: NullPointerException => complete((BadRequest,
      Error(BadRequest.intValue, BadRequest.defaultMessage, e.getMessage)))
    case e: RuntimeException => complete((InternalServerError,
      Error(InternalServerError.intValue, InternalServerError.defaultMessage,
        e.getMessage)))
    case e: Exception => complete((InternalServerError, Error(InternalServerError.intValue,
      InternalServerError.defaultMessage, e.getMessage)))
  }
}
