package endpoints

import _root_.repository.UserRepository
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{entity, get, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import models._
import org.bson.types.ObjectId
import util.StringUtil.toSlug

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class UserEndpoint(repository: UserRepository)(implicit ec: ExecutionContext, mat: Materializer) {
  val userRoutes: Route =
    path("messages") {
      ( get & path(Segment).as(FindByIdRequest) ) { request =>
        onComplete(repository.findByName(request.id)) {
          case Success(Some(tweet)) =>
            val tags = tweet.message.split(" ").filter(_.startsWith("#")).flatMap { tag =>
              Seq(Tags(tag.replace("#", ""), toSlug(tag)))
            }
            val response = Response(tweet._id.toHexString, tweet.username, tweet.message, tags)
            complete(Marshal(response).to[ResponseEntity].map { e => HttpResponse(status = StatusCodes.OK, entity = e) })
          case Success(None) =>
            complete(HttpResponse(status = StatusCodes.NotFound))
          case Failure(e) =>
            complete(Marshal(Message(e.getMessage)).to[ResponseEntity].map { e => HttpResponse(entity = e, status = StatusCodes.InternalServerError) })
        }
      } ~ (post & pathEndOrSingleSlash & extractRequest) { request =>
        entity(as[RequestBody]) { body=>
        onComplete(repository.save(Tweet(ObjectId.get(), request.headers.map(_.value()).head, body.message))) {
          case Success(rs) =>
            complete(HttpResponse(status = StatusCodes.Created, entity = HttpEntity(ContentTypes.`application/json`, rs.asJson.toString())))
          case Failure(e) =>
            complete(Marshal(Message(e.getMessage)).to[ResponseEntity].map { e => HttpResponse(entity = e, status = StatusCodes.InternalServerError) })
        }
      }
      } ~ (delete & path(Segment).as(FindByIdRequest)) { id =>
        onComplete(repository.deleteById(id.id)) {
          case Success(value) if value >= 1 => complete(HttpResponse(status = StatusCodes.Accepted))
          case Success(_) =>
            complete(HttpResponse(status = StatusCodes.NotFound))
          case Failure(e) =>
            complete(Marshal(Message(e.getMessage)).to[ResponseEntity].map { e => HttpResponse(entity = e, status = StatusCodes.InternalServerError) })

        }
      } ~ (get & pathEndOrSingleSlash & extractRequest) { request =>
        parameters("tag".as[String].?, "page".as[Int].?, "count".as[Int].?) { (tag, page, count) =>
          val tags = tag.get
          val pages = page.getOrElse(1)
          val counts = count.getOrElse(10)

          onComplete(repository.findByParameters(request.headers.map(_.value()).head, tags, counts, pages)) {
            case Success(Some(responses)) if responses.nonEmpty =>
              val response = responses.map { response =>
                val tags = responses.flatMap(_.message.split(" ").filter(_.startsWith("#"))).flatMap { messageEntity =>
                  Seq(Tags(messageEntity, toSlug(messageEntity)))
                }
                Response(response._id.toHexString, response.username, response.message, tags)
              }
              complete(Marshal(response).to[ResponseEntity].map { e => HttpResponse(entity = e) })
            case Success(_) =>
              complete(HttpResponse(status = StatusCodes.NotFound))
            case Failure(e) =>
              complete(Marshal(Message(e.getMessage)).to[ResponseEntity].map { e => HttpResponse(entity = e, status = StatusCodes.InternalServerError) })
          }
        }
      }
    }
}

