package features

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import modules.AllModulesTest
import org.scalatest.{BeforeAndAfterAll, FeatureSpec, Matchers}

class UserEndpointFeature
  extends FeatureSpec
    with Matchers
    with ScalatestRouteTest
    with BeforeAndAfterAll {

  val modules = new AllModulesTest

  val routes = modules.endpoints.routes

  val httpEntity: (String) => HttpEntity.Strict = (str: String) => HttpEntity(ContentTypes.`application/json`, str)

  feature("messages") {
    scenario("success creation") {
      val validUser =
        """
          {
            "message": "gabfssilva"
          }
        """
      Post(s"/messages", httpEntity(validUser)) ~> addHeader("username", "kemal") ~> routes ~> check {
        status shouldBe StatusCodes.Created
      }
    }

    scenario("no headers") {
      val validUser =
        """
          {
            "message": "denme"
           }
        """
      Post("/messages", httpEntity(validUser)) ~> routes ~> check {
        status shouldBe StatusCodes.InternalServerError

      }
    }


    scenario("no body") {
      Post("/messages", httpEntity("{}")) ~> routes ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

  }
}
