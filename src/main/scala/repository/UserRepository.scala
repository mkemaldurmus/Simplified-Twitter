package repository

import models.{Response, Tags, Tweet}
import org.bson.types.ObjectId
import org.mongodb.scala._
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts._
import util.StringUtil.toSlug

import scala.concurrent.{ExecutionContext, Future}

class UserRepository(collection: MongoCollection[Tweet])(implicit ec: ExecutionContext) {

  def findByName(userName: String): Future[Option[Tweet]] =
    collection
      .find(Document("_id" -> new ObjectId(userName)))
      .first()
      .head()
      .map(Option(_))

  def findByParameters(userName: String, tag: String, count: Int, pageNumber: Int): Future[Option[List[Tweet]]] =
    collection
      .find(or(Filters.eq("username", userName), Filters.regex("message", tag)))
      .skip((pageNumber - 1) * 5)
      .sort(descending("id"))
      .toFuture()
      .map(_.take(count))
      .map(_.toList)
      .map(Option(_))


  def deleteById(id: String): Future[Long] =
    collection.deleteOne(Document("_id" -> new ObjectId(id))).head().map(_.getDeletedCount)

  def save(tweet: Tweet): Future[Response] = {
    collection
      .insertOne(tweet)
      .map { _ =>
        val tags: Seq[Tags] = tweet.message.split(" ").filter(_.startsWith("#")).flatMap { tag =>
          Seq(Tags(tag.replace("#", ""), toSlug(tag)))
        }
        Response(tweet._id.toHexString, tweet.username, tweet.message, tags)
      }.head()
  }
}
