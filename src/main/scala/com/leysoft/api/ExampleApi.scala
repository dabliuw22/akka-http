package com.leysoft.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._

import scala.collection.mutable.Map
import scala.collection.immutable.List
import scala.concurrent.{ExecutionContext, Future}

object ExampleApi extends App {
  implicit val system = ActorSystem("example-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val `host` = "localhost"
  val `port` = 8080
  system.log

  val authorRouter = AuthorRouter(AuthorRepository(), system.log).route

  Http().bindAndHandle(authorRouter, `host`, `port`)
}

final case class Author(id: Int, name: String)

case class AuthorRouter(authorRepository: AuthorRepository, logger: LoggingAdapter)
                       (implicit executionContext: ExecutionContext) extends SprayJsonSupport {

  implicit val format = jsonFormat2(Author)
  import spray.json._

  def route: Route = path("authors") {
    concat(getById, getAll, create, update)
  }

  private def getById: Route = (get & parameters("id".as[Int])) { id =>
    logger.info(s"GET - id: $id")
    val author = authorRepository.findById(id)
    logger.info(s"Author: ${author.toJson.prettyPrint}")
    complete(author)
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
    2 -> Author(1, "Author2"),
    3 -> Author(3, "Author3"))

  def save(author: Author): Future[Author] = Future(authors.put(author.id, author))
    .map { result => author }

  def findById(id: Int): Option[Author] = authors.get(id)

  def findAll: Future[List[Author]] = Future(List.from(authors.values))
}
