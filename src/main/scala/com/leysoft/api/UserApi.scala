package com.leysoft.api

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.javadsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Source}
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json.DefaultJsonProtocol

import scala.collection.mutable.Map

object UserApi extends App {
  import akka.http.scaladsl.server.Directives._
  implicit val system = ActorSystem("user-system")
  implicit val materialize = ActorMaterializer()
  val `host` = "localhost"
  val `port` = 8080

  implicit val jwtService = JwtService()
  val userService = UserService(UserRepositoryImp())
  val route = concat(UserRouter(userService).route, SecurityRouter(userService).login)

  Http().bindAndHandle(route, `host`, `port`)
}

case class User(username: String, password: String, active: Boolean = true)

case class Status(status: Boolean = false)

case class LoginRequest(username: String, password: String)

case class TokenResponse(token: String)

trait BasicJsonProtocol extends DefaultJsonProtocol {

  implicit val statusFormat = jsonFormat1(Status)
}

trait UserJsonProtocol extends BasicJsonProtocol {

  implicit val format = jsonFormat3(User)
}

trait LoginRequestJsonProtocol extends BasicJsonProtocol {

  implicit val loginFormat = jsonFormat2(LoginRequest)
}

trait TokenResponseJsonProtocol extends DefaultJsonProtocol {

  implicit val tokenFormat = jsonFormat1(TokenResponse)
}

trait JsonStreamingSupport {

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
}

case class UserRouter(userService: UserService) (implicit jwtService: JwtService) extends SprayJsonSupport
  with UserJsonProtocol with JsonStreamingSupport {

  import akka.http.scaladsl.server.Directives._

  private val `prefix` = "users"

  def route: Route = concat(create, getByUsername, getAllActive, deactivate)

  private def create: Route = (path(`prefix`) & post & entity(as[User])) { user =>
    complete(userService.create(user))
  }

  private def getByUsername: Route = (get & path(`prefix` / Segment) & headerValueByName("Authorization")) {
    (username, token) => if (jwtService.validateJwt(token)) complete(userService.getBy(username))
      else complete(StatusCodes.Unauthorized)
  }

  private def getAllActive: Route = (path(`prefix`) & get) {
    complete(userService.getAll)
  }

  private def deactivate: Route = (delete & path(`prefix` / Segment)) { username =>
    complete(userService.delete(username))
  }
}

case class SecurityRouter(userService: UserService) (implicit jwtService: JwtService) extends SprayJsonSupport
  with LoginRequestJsonProtocol with JsonStreamingSupport with TokenResponseJsonProtocol {

  import akka.http.scaladsl.server.Directives._

  def login: Route = (path("login") & post & entity(as[LoginRequest])) { login =>
    complete(userService.getBy(login.username)
      .filter(user => user match {
        case Some(value) => value.password.equals(login.password)
        case _ => false})
      .map(user => TokenResponse(jwtService.cretaeJwt(user.get))))
  }
}

case class JwtService() {

  private val `algorithm` = JwtAlgorithm.HS256

  private val `secretKey` = "yourSecretKey"

  private val `expirationDays` = 1

  private val `bearerPrefix` = "Bearer "

  def cretaeJwt(user: User): String = {
    val currentTimeMillis = System.currentTimeMillis()
    val jwtClaims = JwtClaim(
      subject = Some(user.username),
      issuedAt = Some(currentTimeMillis / 1000),
      expiration = Some(currentTimeMillis / 1000 + TimeUnit.DAYS.toSeconds(`expirationDays`)))
    JwtSprayJson.encode(jwtClaims, `secretKey`, `algorithm`)
  }

  def validateJwt(jwt: String) : Boolean = JwtSprayJson
    .isValid(jwt.replace(`bearerPrefix`, ""), secretKey, Seq(algorithm))
}

case class UserService(userRepository: UserRepository) {

  def create(user: User) = userRepository.save(user)

  def getBy(username: String) = userRepository.findBy(username)

  def getAll = userRepository.findAll

  def delete(username: String) = userRepository.delete(username)
}

trait UserRepository {

  def save(user: User): Source[Option[User], NotUsed]

  def findBy(username: String): Source[Option[User], NotUsed]

  def findAll: Source[User, NotUsed]

  def delete(username: String): Source[Status, NotUsed]
}

case class UserRepositoryImp()(implicit materialize: ActorMaterializer) extends UserRepository {

  private val users = Map[String, User](
    "username1" -> User("username1", "password1"),
    "username2" -> User("username2", "password2"),
    "username3" -> User("username3", "password3"),
  )

  override def save(user: User): Source[Option[User], NotUsed] = Source.single(user)
    .via(Flow[User].map { u => {
        users.put(u.username, u) match {
          case Some(value) => Some(value)
          case None => Some(u)
        }
      }
    })

  override def findBy(username: String): Source[Option[User], NotUsed] = Source.single(username)
    .via(Flow[String].map { u => users.get(u) })

  override def findAll: Source[User, NotUsed] = Source(List.from(users.values
    .filter { user => user.active }))

  override def delete(username: String): Source[Status, NotUsed] = Source.single(username)
    .map(u => {
      users.get(u) match {
        case Some(value) => {
          val newUser = User(username, value.password, false)
          users.put(username, newUser)
          Status(true)
        }
        case _ => Status()
      }
    }).async
}