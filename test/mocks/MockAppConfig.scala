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

package mocks

import config.{AppConfig, ConfidenceLevelConfig}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.Configuration
import routing.Version

trait MockAppConfig extends MockFactory {

  val mockAppConfig: AppConfig = mock[AppConfig]

  object MockedAppConfig {
    // DES Config
    def desBaseUrl: CallHandler[String]                         = (() => mockAppConfig.desBaseUrl).expects()
    def desToken: CallHandler[String]                           = (() => mockAppConfig.desToken).expects()
    def desEnvironment: CallHandler[String]                     = (() => mockAppConfig.desEnv).expects()
    def desEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.desEnvironmentHeaders).expects()

    // IFS Config
    def ifsBaseUrl: CallHandler[String]                         = (() => mockAppConfig.ifsBaseUrl).expects()
    def ifsToken: CallHandler[String]                           = (() => mockAppConfig.ifsToken).expects()
    def ifsEnvironment: CallHandler[String]                     = (() => mockAppConfig.ifsEnv).expects()
    def ifsEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.ifsEnvironmentHeaders).expects()

    // Tax Year Specific IFS Config
    def tysIfsBaseUrl: CallHandler[String]                         = (() => mockAppConfig.tysIfsBaseUrl).expects()
    def tysIfsToken: CallHandler[String]                           = (() => mockAppConfig.tysIfsToken).expects()
    def tysIfsEnvironment: CallHandler[String]                     = (() => mockAppConfig.tysIfsEnv).expects()
    def tysIfsEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.tysIfsEnvironmentHeaders).expects()

    // Release6 Config
    def release6BaseUrl: CallHandler[String]                         = (() => mockAppConfig.release6BaseUrl).expects()
    def release6Token: CallHandler[String]                           = (() => mockAppConfig.release6Token).expects()
    def release6Environment: CallHandler[String]                     = (() => mockAppConfig.release6Env).expects()
    def release6EnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.release6EnvironmentHeaders).expects()

    // api1661 Config
    def api1661BaseUrl: CallHandler[String]                         = (() => mockAppConfig.api1661BaseUrl).expects()
    def api1661Token: CallHandler[String]                           = (() => mockAppConfig.api1661Token).expects()
    def api1661Environment: CallHandler[String]                     = (() => mockAppConfig.api1661Env).expects()
    def api1661EnvironmentHeaders: CallHandler[Option[Seq[String]]] = (() => mockAppConfig.api1661EnvironmentHeaders).expects()

    // MTD IF Lookup Config
    def mtdIdBaseUrl: CallHandler[String]                        = (() => mockAppConfig.mtdIdBaseUrl).expects()
    def featureSwitches: CallHandler[Configuration]              = (() => mockAppConfig.featureSwitches).expects()
    def apiGatewayContext: CallHandler[String]                   = (() => mockAppConfig.apiGatewayContext).expects()
    def apiStatus(version: Version): CallHandler[String]         = (mockAppConfig.apiStatus(_: Version)).expects(version)
    def endpointsEnabled(version: Version): CallHandler[Boolean] = (mockAppConfig.endpointsEnabled(_: Version)).expects(version)
    def minimumPermittedTaxYear: CallHandler[Int]                = (() => mockAppConfig.minimumPermittedTaxYear).expects()
    def ukDividendsMinimumTaxYear: CallHandler[Int]              = (() => mockAppConfig.ukDividendsMinimumTaxYear).expects()

    def confidenceLevelCheckEnabled: CallHandler[ConfidenceLevelConfig] =
      (() => mockAppConfig.confidenceLevelConfig).expects()

  }

}
