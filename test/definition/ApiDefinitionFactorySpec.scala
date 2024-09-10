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

import api.mocks.MockHttpClient
import definition.APIStatus.{ALPHA, BETA}
import mocks.MockAppConfig
import play.api.Configuration
import routing.{Version1, Version2}
import support.UnitSpec

class ApiDefinitionFactorySpec extends UnitSpec {

  class Test extends MockHttpClient with MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockedAppConfig.apiGatewayContext returns "individuals/income-received"
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        MockedAppConfig.featureSwitches returns Configuration.empty
        MockedAppConfig.apiStatus(Version1) returns "BETA"
        MockedAppConfig.apiStatus(Version2) returns "BETA"
        MockedAppConfig.endpointsEnabled(Version1) returns true
        MockedAppConfig.endpointsEnabled(Version2) returns true

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Individuals Income Received (MTD)",
              description = "An API for providing individual income received data",
              context = "individuals/income-received",
              categories = Seq("INCOME_TAX_MTD"),
              versions = Seq(
                APIVersion(
                  version = Version1,
                  status = BETA,
                  endpointsEnabled = true
                ),
                APIVersion(
                  version = Version2,
                  status = BETA,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      "return the correct status" in new Test {
        MockedAppConfig.apiStatus(Version1) returns "BETA"
        apiDefinitionFactory.buildAPIStatus(Version1) shouldBe BETA
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      "default to alpha" in new Test {
        MockedAppConfig.apiStatus(Version1) returns "ALPHO"
        apiDefinitionFactory.buildAPIStatus(Version1) shouldBe ALPHA
      }
    }
  }

}
