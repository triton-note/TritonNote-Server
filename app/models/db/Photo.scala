package models.db

import scala.collection.JavaConversions._
import java.util.Date

import javax.imageio.ImageIO

import scala.concurrent.duration._

import org.fathens.play.util.Exception.allCatch

import com.amazonaws.services.dynamodbv2.model._

import models.Storage

case class Photo(id: String,
  createdAt: Date,
  lastModifiedAt: Option[Date],
  catchReport: Option[CatchReport],
  image: Option[Image]) {
  /**
   * Reload from DB.
   * If there is no longer me, returns None.
   */
  def refresh: Option[Photo] = Photos.get(id)
  /**
   * Delete me
   */
  def delete: Boolean = Photos.delete(id)
}
object Photos extends AutoIDTable[Photo]("PHOTO") {
  val catchReport = Column[Option[CatchReport]]("CATCH_REPORT", (_.catchReport), (_.get(CatchReports)), attrObjID)
  val image = Column[Option[Image]]("IMAGE", (_.image), (_.get(Images)), attrObjID)
  val columns = List(catchReport, image)
  def fromMap(implicit map: Map[String, AttributeValue]): Option[Photo] = allCatch opt Photo(
    id.build,
    createdAt.build,
    lastModifiedAt.build,
    catchReport.build,
    image.build
  )
  /**
   * Add new photo.
   * Brand new id will be generated and injected into new Photo instance.
   */
  def addNew(theCatchReport: CatchReport, theImage: Image): Photo = addNew(
    catchReport(Option(theCatchReport)),
    image(Option(theImage))
  )
  def findByCatchReport(cr: CatchReport): List[Photo] = {
    find(_.withIndexName("CATCH_REPORT-index").withKeyConditions(Map(
      catchReport compare Option(cr)
    ))).toList
  }
}

case class Image(id: String,
  createdAt: Date,
  lastModifiedAt: Option[Date],
  kind: String,
  format: String,
  dataSize: Long,
  width: Long,
  height: Long) {
  /**
   * Reload from DB.
   * If there is no longer me, returns None.
   */
  def refresh: Option[Image] = Images.get(id)
  /**
   * Delete me
   */
  def delete: Boolean = {
    file.delete && Images.delete(id) 
  }
  lazy val file = Storage.file("photo", kind, id.toString)
  def url(implicit limit: FiniteDuration = 1 hour) = file.generateURL(limit)
}
object Images extends AutoIDTable[Image]("IMAGE") {
  val kind = Column[String]("KIND", (_.kind), (_.getString.get), attrString)
  val format = Column[String]("FORMAT", (_.format), (_.getString.get), attrString)
  val dataSize = Column[Long]("DATA_SIZE", (_.dataSize), (_.getLong.get), attrLong)
  val width = Column[Long]("WIDTH", (_.width), (_.getLong.get), attrLong)
  val height = Column[Long]("HEIGHT", (_.height), (_.getLong.get), attrLong)
  // All columns
  val columns = List(kind, format, dataSize, width, height)
  def fromMap(implicit map: Map[String, AttributeValue]): Option[Image] = allCatch opt Image(
    id.build,
    createdAt.build,
    lastModifiedAt.build,
    kind.build,
    format.build,
    dataSize.build,
    width.build,
    height.build
  )
  /**
   * Add new image data
   */
  def addNew(imageFile: java.io.File): Option[Image] = {
    for {
      biOpt <- allCatch opt ImageIO.read(imageFile)
      bi <- Option(biOpt)
      image = addNew(imageFile.length, bi.getWidth, bi.getHeight)
    } yield {
      image.file write imageFile
      image
    }
  }
  def addNew(theDataSize: Long, theWidth: Long, theHeight: Long,
    theFormat: String = "JPEG", theKind: String = KIND_ORIGINAL): Image = addNew(
    kind(theKind),
    format(theFormat),
    dataSize(theDataSize),
    width(theWidth),
    height(theHeight)
  )
  val KIND_ORIGINAL = "original"
}

case class ImageRelation(id: String,
  createdAt: Date,
  lastModifiedAt: Option[Date],
  imageSrc: Option[Image],
  imageDst: Option[Image],
  relation: String) {
  /**
   * Reload from DB.
   * If there is no longer me, returns None.
   */
  def refresh: Option[ImageRelation] = ImageRelations.get(id)
  /**
   * Delete me
   */
  def delete: Boolean = ImageRelations.delete(id)
}
object ImageRelations extends AutoIDTable[ImageRelation]("IMAGE_RELATION") {
  val imageSrc = Column[Option[Image]]("IMAGE_SRC", (_.imageSrc), (_.get(Images)), attrObjID)
  val imageDst = Column[Option[Image]]("IMAGE_DST", (_.imageDst), (_.get(Images)), attrObjID)
  val relation = Column[String]("RELATION", (_.relation), (_.getString.get), attrString)
  // All columns
  val columns = List(imageSrc, imageDst, relation)
  def fromMap(implicit map: Map[String, AttributeValue]): Option[ImageRelation] = allCatch opt ImageRelation(
    id.build,
    createdAt.build,
    lastModifiedAt.build,
    imageSrc.build,
    imageDst.build,
    relation.build
  )
  /**
   * Add new relation
   */
  def addNew(theImageSrc: Image, theImageDst: Image, theRelation: String): ImageRelation = addNew(
    imageSrc(Option(theImageSrc)),
    imageDst(Option(theImageDst)),
    relation(theRelation)
  )
}
