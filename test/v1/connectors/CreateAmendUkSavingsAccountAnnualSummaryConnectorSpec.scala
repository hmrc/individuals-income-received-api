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

import api.connectors.ConnectorSpec
import api.mocks.MockHttpClient
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.request.createAmendUkSavingsAnnualSummary.{CreateAmendUkSavingsAnnualSummaryBody, CreateAmendUkSavingsAnnualSummaryRequest}

import scala.concurrent.Future

class CreateAmendUkSavingsAccountAnnualSummaryConnectorSpec extends  ConnectorSpec {

    val nino: String       = "AA111111A"
    val taxYearMtd: String = "2018-19"
    val taxYear: TaxYear  =  TaxYear.fromMtd(taxYearMtd)
    val incomeSourceId:    String     = "ABCDE1234567890"

    val taxedUkInterest:   Option[BigDecimal] = Some(31554452289.99)
    val untaxedUkInterest: Option[BigDecimal] = Some(91523009816.00)

    val transactionReference: String  = "0000000000000001"

    val body: CreateAmendUkSavingsAnnualSummaryBody = CreateAmendUkSavingsAnnualSummaryBody(taxedUkInterest, untaxedUkInterest)
    val request = CreateAmendUkSavingsAnnualSummaryRequest(Nino(nino), taxYear,   incomeSourceId, body)


    private val validResponse:JsObject = JSON(s"{\"transactionReference\":\"$transactionReference\"}")


  class Test extends MockHttpClient with MockAppConfig {

    val connector: CreateAmendUkSavingsAccountAnnualSummaryConnector = new CreateAmendUkSavingsAccountAnnualSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "CreateAmendUkSavingsAccountSummaryConnector" when {
    "createAmendUkSavingsAccountAnnualSummary" must {
      "return a 200 status for a success scenario" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, validResponse))
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

        MockedHttpClient
          .post(
             url = s"${baseUrl}/income-tax/nino/${nino}/income-source/savings/annual/${taxYear.toDownstream}",
             config = dummyDesHeaderCarrierConfig,
             requiredHeaders = requiredDesHeaders,
             excludedHeaders = Seq("AnotherHeader" -> "HeaderValue"),
             body = "" //TODO - request model
          )
          .returns(Future.successful(outcome))
        await(connector.createOrAmendUKSavingsAccountSummary(validRequest))
      }
    }
  }

}
