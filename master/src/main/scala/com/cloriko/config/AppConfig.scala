package com.cloriko.config

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

import com.cloriko.config.AppConfig.WebServerConfiguration
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.syntax._
import pureconfig._
import pureconfig.configurable.localDateConfigConvert
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

import scala.concurrent.duration.FiniteDuration

case class AppConfig(server: WebServerConfiguration) {
  def toJson: String = this.asJson.noSpaces
}

object AppConfig {

  implicit val confHint: ProductHint[AppConfig] = ProductHint[AppConfig](ConfigFieldMapping(CamelCase, KebabCase))

  implicit val localDateConvert: ConfigConvert[LocalDate] = localDateConfigConvert(ISO_DATE)

  implicit val encodeAppConfig: Encoder[AppConfig] = deriveEncoder
  implicit val encodeDuration: Encoder[FiniteDuration] = Encoder.instance(duration ⇒ Json.fromString(duration.toString))
  implicit val encodeLocalDate: Encoder[LocalDate] = Encoder.instance(date ⇒ Json.fromString(date.format(ISO_DATE)))

  def load(): AppConfig = loadConfigOrThrow[AppConfig]

  case class WebServerConfiguration(
    host: String,
    port: Int,
    endPoint: String)

}

