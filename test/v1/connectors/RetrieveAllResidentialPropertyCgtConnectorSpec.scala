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

package v1.connectors

import api.connectors.DownstreamUri.DesUri
import api.connectors.ConnectorSpec
import api.mocks.MockHttpClient
import api.models.domain.{MtdSourceEnum, Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.retrieveAllResidentialPropertyCgt.RetrieveAllResidentialPropertyCgtRequest

import scala.concurrent.Future

class RetrieveAllResidentialPropertyCgtConnectorSpec extends ConnectorSpec {

  val nino: String    = "AA111111A"
  val taxYear: String = "2019-20"

  private val retrieveAllResidentialPropertyCgtRequest = RetrieveAllResidentialPropertyCgtRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    source = MtdSourceEnum.latest
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveAllResidentialPropertyCgtConnector = new RetrieveAllResidentialPropertyCgtConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "RetrieveAllResidentialPropertyCgtConnector" when {

    "retrieve" must {
      "return a 200 status for a success scenario" in new Test {

        case class Data(field: String)

        object Data {
          implicit val reads: Reads[Data] = Json.reads[Data]
        }

        val outcome                       = Right(ResponseWrapper(correlationId, Data("value")))
        implicit val desUri: DesUri[Data] = DesUri[Data](s"income-tax/income/savings/$nino/$taxYear")
        implicit val hc: HeaderCarrier    = HeaderCarrier(otherHeaders = otherHeaders)

        MockedHttpClient
          .get(
            url = s"$baseUrl/income-tax/income/savings/$nino/$taxYear",
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieve(retrieveAllResidentialPropertyCgtRequest)) shouldBe outcome
      }
    }
  }

}
