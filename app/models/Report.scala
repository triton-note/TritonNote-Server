package models

import java.util.Date

import play.api.libs.json._

case class Report(
  id: Option[String],
  comment: String,
  dateAt: Date,
  location: Report.Location,
  condition: Report.Condition,
  photo: Option[Report.Photo],
  fishes: Seq[Report.Fishes]) {
}
object Report {
  case class Location(name: String, geoinfo: GeoInfo)
  object Location {
    implicit val locationFormat = Json.format[Location]
  }
  case class Condition(moon: Int, tide: Condition.Tide.Value, weather: Option[Condition.Weather]) {
    lazy val asJson = Json toJson this
  }
  object Condition {
    object Tide extends Enumeration {
      val Flood = Value("Flood")
      val High = Value("High")
      val Ebb = Value("Ebb")
      val Low = Value("Low")
      implicit val tideFormat = Format(
        (__).read[String].map(Tide.withName),
        Writes { (t: Tide.Value) => JsString(t.toString) })
    }
    case class Weather(name: String, temperature: ValueUnit.Temperature, iconUrl: String)
    object Weather {
      implicit val weatherFormat = Json.format[Weather]
    }
    implicit val json = Json.format[Condition]
  }
  case class Photo(
    original: String,
    mainview: String,
    thumbnail: String)
  object Photo {
    implicit val photoFormat = Json.format[Photo]
  }
  case class Fishes(
    name: String,
    count: Int,
    weight: Option[ValueUnit.Weight] = None,
    length: Option[ValueUnit.Length] = None) {
  }
  object Fishes {
    object SizeValue {
      implicit val json = Json.format[SizeValue]
    }
    case class SizeValue(weight: Option[ValueUnit.Weight], length: Option[ValueUnit.Length]) {
      override def toString = List(weight, length).flatten.map(_.toString) match {
        case Nil  => ""
        case list => list.mkString(", ")
      }
      lazy val asJson = Json toJson this
    }
    implicit val catchesFormat = Json.format[Fishes]
  }
  implicit val reportFormat = Json.format[Report]
}
