package models.db

import java.util.Date

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.concurrent.duration._

import scalaz.Scalaz._

import play.api.Logger

import org.fathens.play.util.Exception.allCatch

import com.amazonaws.services.dynamodbv2.model._

case class VolatileToken(id: String,
  createdAt: Date,
  lastModifiedAt: Option[Date],
  expiration: Date,
  extra: Option[String]) {
  /**
   * Reload form DB
   */
  def refresh: Option[VolatileToken] = VolatileTokens.get(id)
  /**
   * Delete me
   */
  def delete: Boolean = VolatileTokens.delete(id)
  /**
   * Change properties (like a copy) and update Database
   */
  def update(extra: Option[String] = this.extra, expiration: Date = this.expiration): Option[VolatileToken] = {
    VolatileTokens.update(id, Map(
      VolatileTokens.extra(extra),
      VolatileTokens.expiration(expiration)
    ))(for {
      (n, v) <- Map(VolatileTokens.extra(extra))
    } yield n -> new ExpectedAttributeValue(v).withComparisonOperator(ComparisonOperator.EQ))
  }
  def setExtra(text: String) = update(extra = Some(text))
  def removeExtra = update(extra = None)
  def changeExpiration(theNext: Date) = update(expiration = theNext)
}
object VolatileTokens extends AnyIDTable[VolatileToken]("VOLATILE_TOKEN") {
  val expiration = Column[Date]("EXPIRATION", (_.expiration), (_.getDate.get), attrDate)
  val extra = Column[Option[String]]("EXTRA", (_.extra), (_.getString), attrString)
  // All columns
  val columns = List(expiration, extra)
  def fromMap(implicit map: Map[String, AttributeValue]): Option[VolatileToken] = allCatch opt VolatileToken(
    id.build,
    createdAt.build,
    lastModifiedAt.build,
    expiration.build,
    extra.build
  )
  /**
   * Add new token
   */
  def addNew(theToken: String, willExpired: FiniteDuration, theExtra: Option[String] = None): Option[VolatileToken] = addNew(theToken,
    expiration(new Date(currentTimestamp.getTime + willExpired.toMillis)),
    extra(theExtra)
  )
  /**
   * Create new token and save to database
   */
  @tailrec
  def createNew(willExpired: FiniteDuration, extra: Option[String] = None): VolatileToken = {
    val token = play.api.libs.Codecs.sha1(System.currentTimeMillis.toString)
    addNew(token, willExpired, extra) match {
      case None    => createNew(willExpired, extra)
      case Some(a) => a
    }
  }
  /**
   * Delete expired tokens
   * @return number of deleted
   */
  def deleteExpired: Int = {
    val expired = scan {
      Map(expiration(currentTimestamp)).map {
        case (n, v) => n -> new Condition().withComparisonOperator(ComparisonOperator.LE).withAttributeValueList(v)
      }
    }
    expired.toList.map(_.delete).filter(_ == true).size
  }
}
