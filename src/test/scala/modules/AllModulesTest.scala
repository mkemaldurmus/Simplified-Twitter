package modules

import com.github.fakemongo.async.FongoAsync
import com.mongodb.async.client.MongoDatabase
import com.typesafe.config.ConfigFactory
import models.Tweet
import org.mongodb.scala.MongoCollection

class AllModulesTest extends AllModules {
  val db: MongoDatabase = {
    val fongo = new FongoAsync("akka-http-mongodb-microservice")
    val db = fongo.getDatabase(ConfigFactory.load().getString("mongo.database"))
    db.withCodecRegistry(codecRegistry)
  }

  override lazy val userCollection: MongoCollection[Tweet] = MongoCollection(db.getCollection("users", classOf[Tweet]))
}
