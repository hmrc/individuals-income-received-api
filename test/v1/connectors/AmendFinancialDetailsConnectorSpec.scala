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
import v1.models.request.amendFinancialDetails.emploment.{AmendEmployment, AmendPay}
import v1.models.request.amendFinancialDetails.{AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}

import scala.concurrent.Future

class AmendFinancialDetailsConnectorSpec extends ConnectorSpec {

  "AmendFinancialDetailsConnector" should {
    "return a 204 status for a success scenario" when {
      "a valid request is submitted" in new Release6Test with Test {
        def taxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/income/employments/$nino/2019-20/$employmentId",
          body = amendFinancialDetailsRequestBody
        ).returns(Future.successful(outcome))

        private val result = await(connector.amendFinancialDetails(amendFinancialDetailsRequest))
        result shouldBe outcome

      }

      "a valid request is submitted for a TYS tax year" in new TysIfsTest with Test {
        def taxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/23-24/income/employments/$nino/$employmentId",
          body = amendFinancialDetailsRequestBody
        ).returns(Future.successful(outcome))

        private val result = await(connector.amendFinancialDetails(amendFinancialDetailsRequest))
        result shouldBe outcome

      }
    }
  }

  trait Test { _: ConnectorTest =>

    val nino: String = "AA111111A"
    val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    protected val connector: AmendFinancialDetailsConnector = new AmendFinancialDetailsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val payModel = AmendPay(
      taxablePayToDate = 3500.75,
      totalTaxToDate = 6782.92
    )

    protected val employmentModel = AmendEmployment(
      pay = payModel,
      deductions = None,
      benefitsInKind = None,
      offPayrollWorker = None
    )

    protected val amendFinancialDetailsRequestBody = AmendFinancialDetailsRequestBody(
      employment = employmentModel
    )

    protected val amendFinancialDetailsRequest: AmendFinancialDetailsRequest =
      AmendFinancialDetailsRequest(Nino(nino), taxYear, employmentId, amendFinancialDetailsRequestBody)

    def taxYear: TaxYear

  }

}
