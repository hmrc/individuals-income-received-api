/*
 * Copyright 2020 HM Revenue & Customs
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

import mocks.MockAppConfig
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.domain.DesTaxYear
import v1.models.outcomes.ResponseWrapper
import v1.models.request.savings.amend.{AmendForeignInterest, AmendSavingsRequest, AmendSavingsRequestBody}

import scala.concurrent.Future

class AmendSavingsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"
  val taxYear: String = "2019"

  val foreignIntrest: AmendForeignInterest = AmendForeignInterest(
    amountBeforeTax = None,
    countryCode = "FRA",
    taxTakenOff = None,
    specialWithholdingTax = None,
    taxableAmount = None,
    foreignTaxCreditRelief = false
  )

  val amendSavingsRequestBody: AmendSavingsRequestBody = AmendSavingsRequestBody(securities = None, foreignInterest = Some(Seq(foreignIntrest)))

  val amendSavingsRequest: AmendSavingsRequest = AmendSavingsRequest(
    nino = Nino(nino),
    taxYear = DesTaxYear(taxYear),
    amendSavingsRequestBody = amendSavingsRequestBody
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendSavingsConnector = new AmendSavingsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "AmendSavingsConnector" when {
    "amendSaving" must {
      "return a 200 status for a success scenario" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/some-placeholder/$nino/$taxYear",
            body = amendSavingsRequestBody,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          ).returns(Future.successful(outcome))

        await(connector.amendSavings(amendSavingsRequest)) shouldBe outcome
      }
    }
  }
}
