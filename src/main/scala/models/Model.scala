package models

import io.circe._
import io.circe.syntax._
import org.bson.types.ObjectId


case class FindByIdRequest(id: String) {
  require(ObjectId.isValid(id), "the informed id is not a representation of a valid hex string")
}

case class Tweet(_id:ObjectId,username: String, message: String) {
  require(username != null, "username not informed")
  require(username.nonEmpty, "username cannot be empty")
}
case class Tags(tag:String, slug:String)

case class RequestBody(message: String)


case class Response(_id:String, username:String, message:String, tags:Seq[Tags])

object Tags {
  implicit val encoder: Encoder[Tags] = (a: Tags) => {
    Json.obj(
      "tag" -> a.tag.asJson,
      "slug" -> a.slug.asJson
    )
  }
  implicit val decoder: Decoder[Tags] = (c: HCursor) => {
    for {
      tag <- c.downField("tag").as[String]
      slug <- c.downField("slug").as[String]
    } yield Tags(tag,slug)
  }
}
object RequestBody {
  implicit val encoder: Encoder[RequestBody] = (a: RequestBody) => {
    Json.obj(
      "message" -> a.message.asJson
    )
  }
  implicit val decoder: Decoder[RequestBody] = (c: HCursor) => {
    for {
      message <- c.downField("message").as[String]
    } yield RequestBody(message)
  }
}


object Tweet {
  implicit val encoder: Encoder[Tweet] = (a: Tweet) => {
    Json.obj(
      "id" -> a._id.toHexString.asJson,
      "username" -> a.username.asJson,
      "message" -> a.message.asJson
    )
  }

  implicit val decoder: Decoder[Tweet] = (c: HCursor) => {
    for {
      username <- c.downField("username").as[String]
      message <- c.downField("message").as[String]
    } yield Tweet(ObjectId.get(),username, message)
  }
}

case class Message(message: String)

object Message {
  implicit val encoder: Encoder[Message] = m => Json.obj("message" -> m.message.asJson)
}