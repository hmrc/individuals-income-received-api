/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.typesafe.config.Config
import play.api.{ConfigLoader, Configuration}
import routing.Version
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

trait AppConfig {

  lazy val desDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = desBaseUrl, env = desEnv, token = desToken, environmentHeaders = desEnvironmentHeaders)

  lazy val ifsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = ifsBaseUrl, env = ifsEnv, token = ifsToken, environmentHeaders = ifsEnvironmentHeaders)

  lazy val taxYearSpecificIfsDownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = tysIfsBaseUrl, env = tysIfsEnv, token = tysIfsToken, environmentHeaders = tysIfsEnvironmentHeaders)

  lazy val release6DownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = release6BaseUrl, env = release6Env, token = release6Token, environmentHeaders = release6EnvironmentHeaders)

  lazy val api1661DownstreamConfig: DownstreamConfig =
    DownstreamConfig(baseUrl = api1661BaseUrl, env = api1661Env, token = api1661Token, environmentHeaders = api1661EnvironmentHeaders)

  def mtdIdBaseUrl: String

  // DES Config
  def desBaseUrl: String

  def desEnv: String

  def desToken: String

  def desEnvironmentHeaders: Option[Seq[String]]

  // IFS Config
  def ifsBaseUrl: String

  def ifsEnv: String

  def ifsToken: String

  def ifsEnvironmentHeaders: Option[Seq[String]]

  // Tax Year Specific (TYS) IFS Config
  def tysIfsBaseUrl: String

  def tysIfsEnv: String

  def tysIfsToken: String

  def tysIfsEnvironmentHeaders: Option[Seq[String]]

  // release6 Config
  def release6BaseUrl: String

  def release6Env: String

  def release6Token: String

  def release6EnvironmentHeaders: Option[Seq[String]]

  // Api1661 Config
  def api1661BaseUrl: String

  def api1661Env: String

  def api1661Token: String

  def api1661EnvironmentHeaders: Option[Seq[String]]

  def apiGatewayContext: String
  def minimumPermittedTaxYear: Int
  def ukDividendsMinimumTaxYear: Int

  // API Config
  def apiStatus(version: Version): String
  def featureSwitches: Configuration
  def endpointsEnabled(version: Version): Boolean

  def confidenceLevelConfig: ConfidenceLevelConfig

  // NRS Config
  def mtdNrsProxyBaseUrl: String
}

@Singleton
class AppConfigImpl @Inject() (config: ServicesConfig, configuration: Configuration) extends AppConfig {

  val mtdIdBaseUrl: String = config.baseUrl("mtd-id-lookup")

  // DES Config
  val desBaseUrl: String                         = config.baseUrl("des")
  val desEnv: String                             = config.getString("microservice.services.des.env")
  val desToken: String                           = config.getString("microservice.services.des.token")
  val desEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.des.environmentHeaders")

  // IFS Config
  val ifsBaseUrl: String                         = config.baseUrl("ifs")
  val ifsEnv: String                             = config.getString("microservice.services.ifs.env")
  val ifsToken: String                           = config.getString("microservice.services.ifs.token")
  val ifsEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.ifs.environmentHeaders")

  // Tax Year Specific (TYS) IFS Config
  val tysIfsBaseUrl: String                         = config.baseUrl("tys-ifs")
  val tysIfsEnv: String                             = config.getString("microservice.services.tys-ifs.env")
  val tysIfsToken: String                           = config.getString("microservice.services.tys-ifs.token")
  val tysIfsEnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.tys-ifs.environmentHeaders")

  // Release6 Config
  val release6BaseUrl: String                         = config.baseUrl("release6")
  val release6Env: String                             = config.getString("microservice.services.release6.env")
  val release6Token: String                           = config.getString("microservice.services.release6.token")
  val release6EnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.release6.environmentHeaders")

  // API1661 Config
  val api1661BaseUrl: String                         = config.baseUrl("api1661")
  val api1661Env: String                             = config.getString("microservice.services.api1661.env")
  val api1661Token: String                           = config.getString("microservice.services.api1661.token")
  val api1661EnvironmentHeaders: Option[Seq[String]] = configuration.getOptional[Seq[String]]("microservice.services.api1661.environmentHeaders")

  val apiGatewayContext: String                    = config.getString("api.gateway.context")
  val minimumPermittedTaxYear: Int                 = config.getInt("minimumPermittedTaxYear")
  val ukDividendsMinimumTaxYear: Int               = config.getInt("ukDividendsMinimumTaxYear")
  val confidenceLevelConfig: ConfidenceLevelConfig = configuration.get[ConfidenceLevelConfig](s"api.confidence-level-check")
  // NRS Config
  val mtdNrsProxyBaseUrl: String = config.baseUrl("mtd-api-nrs-proxy")

  // API Config
  def apiStatus(version: Version): String = config.getString(s"api.${version.name}.status")

  def featureSwitches: Configuration = configuration.getOptional[Configuration](s"feature-switch").getOrElse(Configuration.empty)

  def endpointsEnabled(version: Version): Boolean = config.getBoolean(s"api.${version.name}.endpoints.enabled")
}

case class ConfidenceLevelConfig(confidenceLevel: ConfidenceLevel, definitionEnabled: Boolean, authValidationEnabled: Boolean)

object ConfidenceLevelConfig {

  implicit val configLoader: ConfigLoader[ConfidenceLevelConfig] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    ConfidenceLevelConfig(
      confidenceLevel = ConfidenceLevel.fromInt(config.getInt("confidence-level")).getOrElse(ConfidenceLevel.L200),
      definitionEnabled = config.getBoolean("definition.enabled"),
      authValidationEnabled = config.getBoolean("auth-validation.enabled")
    )
  }

}
