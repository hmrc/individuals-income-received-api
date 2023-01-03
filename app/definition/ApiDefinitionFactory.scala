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

package definition

import config.AppConfig
import definition.Versions._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.auth.core.ConfidenceLevel

@Singleton
class ApiDefinitionFactory @Inject() (appConfig: AppConfig) {

  private val readScope      = "read:self-assessment"
  private val writeScope     = "write:self-assessment"
  private val logger: Logger = Logger(this.getClass)

  def confidenceLevel: ConfidenceLevel = if (appConfig.confidenceLevelConfig.definitionEnabled) ConfidenceLevel.L200 else ConfidenceLevel.L50

  lazy val definition: Definition =
    Definition(
      scopes = Seq(
        Scope(
          key = readScope,
          name = "View your Self Assessment information",
          description = "Allow read access to self assessment data",
          confidenceLevel = confidenceLevel
        ),
        Scope(
          key = writeScope,
          name = "Change your Self Assessment information",
          description = "Allow write access to self assessment data",
          confidenceLevel = confidenceLevel
        )
      ),
      api = APIDefinition(
        name = "Individuals Income Received (MTD)",
        description = "An API for providing individual income received data",
        context = appConfig.apiGatewayContext,
        categories = Seq("INCOME_TAX_MTD"),
        versions = Seq(
          APIVersion(
            version = VERSION_1,
            status = buildAPIStatus(VERSION_1),
            endpointsEnabled = appConfig.endpointsEnabled(VERSION_1)
          )
        ),
        requiresTrust = None
      )
    )

  private[definition] def buildAPIStatus(version: String): APIStatus = {
    APIStatus.parser
      .lift(appConfig.apiStatus(version))
      .getOrElse {
        logger.error(s"[ApiDefinition][buildApiStatus] no API Status found in config.  Reverting to Alpha")
        APIStatus.ALPHA
      }
  }

}
