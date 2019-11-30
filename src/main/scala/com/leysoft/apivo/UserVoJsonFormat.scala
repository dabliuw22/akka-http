package com.leysoft.apivo

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

object UserVoJsonFormat extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object UserIdJsonFormat extends JsonFormat[UserId] {

    override def read(json: JsValue): UserId = json match {
      case JsString(value) => UserId(value)
      case _ => throw DeserializationException("UserId Deseralization Error")
    }

    override def write(obj: UserId): JsValue = JsString(obj.value.toString)
  }

  implicit object UserNameJsonFormat extends JsonFormat[UserName] {
    override def read(json: JsValue): UserName = json match {
      case JsString(value) => UserName(value)
      case _ => throw DeserializationException("UserName Deseralization Error")
    }

    override def write(obj: UserName): JsValue = JsString(obj.value.toString)
  }

  implicit val userVoFormat: RootJsonFormat[UserVo] = jsonFormat2(UserVo.apply(_: UserId, _: UserName))
}
