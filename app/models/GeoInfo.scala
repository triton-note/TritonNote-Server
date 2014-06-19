package models

import scala.math.BigDecimal

/**
 * Hold geographic location, latitude and longitude, in degrees.
 */
case class GeoInfo(latitude: BigDecimal, longitude: BigDecimal) {
  import GeoInfo._
  object radian {
    lazy val lat = latitude.toDouble.toRadians
    lazy val lng = longitude.toDouble.toRadians
  }
  def distanceTo(o: GeoInfo) = hubeny.dictance(this, o)
}
object GeoInfo {
  def apply(a: Option[Double], o: Option[Double]): Option[GeoInfo] = for {
    la <- a
    lo <- o
  } yield apply(la, lo)
  /**
   * Estimation of distance by Hubeny's formula
   */
  object hubeny {
    import math._
    /**
     * Calculate distance in meter
     */
    def dictance(g1: GeoInfo, g2: GeoInfo): Double = {
      val (a, b) = {
        val g = db.Geographics.get
        (g.equatorialRadius, g.polarRadius)
      }
      val (x1, y1) = (g1.radian.lng, g1.radian.lat)
      val (x2, y2) = (g2.radian.lng, g2.radian.lat)
      val dx = x1 - x2
      val dy = y1 - y2
      val y = (y1 + y2) / 2
      val e = sqrt((pow(a, 2) - pow(b, 2)) / pow(a, 2))
      val w = sqrt(1 - pow(e, 2) * pow(sin(y), 2))
      val n = a / w
      val m = a * (1 - pow(e, 2)) / pow(w, 3)
      sqrt(pow(dy * m, 2) + pow(dx * n * cos(y), 2))
    }
  }
}
