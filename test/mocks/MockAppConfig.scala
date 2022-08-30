/*
 * Copyright 2022 HM Revenue & Customs
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

trait MockAppConfig extends MockFactory {

  val mockAppConfig: AppConfig = mock[AppConfig]

  object MockedAppConfig {
    // DES Config
    def desBaseUrl: CallHandler[String]                         = (mockAppConfig.desBaseUrl _: () => String).expects()
    def desToken: CallHandler[String]                           = (mockAppConfig.desToken _).expects()
    def desEnvironment: CallHandler[String]                     = (mockAppConfig.desEnv _).expects()
    def desEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (mockAppConfig.desEnvironmentHeaders _).expects()

    // IFS Config
    def ifsBaseUrl: CallHandler[String]                         = (mockAppConfig.ifsBaseUrl _: () => String).expects()
    def ifsToken: CallHandler[String]                           = (mockAppConfig.ifsToken _).expects()
    def ifsEnvironment: CallHandler[String]                     = (mockAppConfig.ifsEnv _).expects()
    def ifsEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (mockAppConfig.ifsEnvironmentHeaders _).expects()

    // Tax Year Specific IFS Config
    def tysIfsBaseUrl: CallHandler[String]                         = (mockAppConfig.tysIfsBaseUrl _: () => String).expects()
    def tysIfsToken: CallHandler[String]                           = (mockAppConfig.tysIfsToken _).expects()
    def tysIfsEnvironment: CallHandler[String]                     = (mockAppConfig.tysIfsEnv _).expects()
    def tysIfsEnvironmentHeaders: CallHandler[Option[Seq[String]]] = (mockAppConfig.tysIfsEnvironmentHeaders _).expects()

    // Release6 Config
    def release6BaseUrl: CallHandler[String]                         = (mockAppConfig.release6BaseUrl _: () => String).expects()
    def release6Token: CallHandler[String]                           = (mockAppConfig.release6Token _).expects()
    def release6Environment: CallHandler[String]                     = (mockAppConfig.release6Env _).expects()
    def release6EnvironmentHeaders: CallHandler[Option[Seq[String]]] = (mockAppConfig.release6EnvironmentHeaders _).expects()

    // api1661 Config
    def api1661BaseUrl: CallHandler[String]                         = (mockAppConfig.api1661BaseUrl _: () => String).expects()
    def api1661Token: CallHandler[String]                           = (mockAppConfig.api1661Token _).expects()
    def api1661Environment: CallHandler[String]                     = (mockAppConfig.api1661Env _).expects()
    def api1661EnvironmentHeaders: CallHandler[Option[Seq[String]]] = (mockAppConfig.api1661EnvironmentHeaders _).expects()

    // MTD IF Lookup Config
    def mtdIdBaseUrl: CallHandler[String] = (mockAppConfig.mtdIdBaseUrl _: () => String).expects()

    def featureSwitches: CallHandler[Configuration] = (mockAppConfig.featureSwitches _: () => Configuration).expects()
    // featureSwitches.returns(Configuration("tys-api.enabled" -> false))

    def apiGatewayContext: CallHandler[String]      = (mockAppConfig.apiGatewayContext _: () => String).expects()
    def apiStatus: CallHandler[String]              = (mockAppConfig.apiStatus: String => String).expects("1.0")
    def endpointsEnabled: CallHandler[Boolean]      = (mockAppConfig.endpointsEnabled: String => Boolean).expects("1.0")
    def minimumPermittedTaxYear: CallHandler[Int]   = (mockAppConfig.minimumPermittedTaxYear _).expects()
    def ukDividendsMinimumTaxYear: CallHandler[Int] = (mockAppConfig.ukDividendsMinimumTaxYear _).expects()

    def confidenceLevelCheckEnabled: CallHandler[ConfidenceLevelConfig] =
      (mockAppConfig.confidenceLevelConfig _: () => ConfidenceLevelConfig).expects()

  }

}
