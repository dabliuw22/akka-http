package com.leysoft.apivo

import java.util.UUID

import akka.http.scaladsl.server.Route

object UserVoRoute {
  import akka.http.scaladsl.server.Directives._
  import UserVoJsonFormat._

  private val users = List(
    UserVo(UserId(UUID.randomUUID()), UserName("user1")),
    UserVo(UserId(UUID.randomUUID()), UserName("user2"))
  )

  private val prefix = "users"

  def route: Route = pathPrefix(prefix) {
    concat(getAll)
  }

  private def getAll: Route = get {
    complete(users)
  }
}