package com.leysoft.apivo

import java.util.UUID

case class UserVo(id: UserId, name: UserName)

object UserVo {

  def apply(id: String, name: String): UserVo = new UserVo(UserId(id), UserName(name))
}

case class UserId(value: UUID)

object UserId {

  def apply(value: String): UserId = new UserId(UUID.fromString(value))
}

case class UserName(value: String)
