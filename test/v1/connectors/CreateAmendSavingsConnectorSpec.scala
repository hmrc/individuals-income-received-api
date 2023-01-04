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

package v1.connectors

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.models.request.amendSavings.{AmendForeignInterestItem, CreateAmendSavingsRequest, CreateAmendSavingsRequestBody}

import scala.concurrent.Future

class CreateAmendSavingsConnectorSpec extends ConnectorSpec {

  "CreateAmendSavingsConnector" when {
    "createAmendSaving" must {
      "return a 204 status for a success scenario" in new IfsTest with Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/savings/$nino/${taxYear.asMtd}",
            config = dummyIfsHeaderCarrierConfig,
            body = requestBody,
            requiredHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.createAmendSavings(request)) shouldBe outcome
      }

      "return a 204 status for a success scenario for Tax Year Specific (TYS)" in new TysIfsTest with Test {
        override def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
        val outcome                   = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/savings/${taxYear.asTysDownstream}/$nino",
            config = dummyIfsHeaderCarrierConfig,
            body = requestBody,
            requiredHeaders,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.createAmendSavings(request)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>
    def nino: String     = "AA111111A"
    def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

    val foreignInterest: AmendForeignInterestItem = AmendForeignInterestItem(
      amountBeforeTax = None,
      countryCode = "FRA",
      taxTakenOff = None,
      specialWithholdingTax = None,
      taxableAmount = 233.11,
      foreignTaxCreditRelief = false
    )

    val requestBody: CreateAmendSavingsRequestBody = CreateAmendSavingsRequestBody(securities = None, foreignInterest = Some(Seq(foreignInterest)))

    val request: CreateAmendSavingsRequest = CreateAmendSavingsRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = requestBody
    )

    val connector: CreateAmendSavingsConnector = new CreateAmendSavingsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

}
