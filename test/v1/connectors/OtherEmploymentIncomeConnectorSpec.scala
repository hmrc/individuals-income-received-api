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
import v1.fixtures.OtherIncomeEmploymentFixture.retrieveResponse
import v1.models.request.otherEmploymentIncome.OtherEmploymentIncomeRequest
import v1.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse

import scala.concurrent.Future

class OtherEmploymentIncomeConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  trait Test {
    _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: OtherEmploymentIncomeConnector =
      new OtherEmploymentIncomeConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

  }

  "OtherEmploymentIncomeConnector" should {
    "return a 200 result on delete" when {
      "the downstream call is successful and not tax year specific" in new DesTest with Test {
        def taxYear: TaxYear                               = TaxYear.fromMtd("2017-18")
        val deleteRequest: OtherEmploymentIncomeRequest    = OtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/other/employments/$nino/2017-18") returns Future.successful(outcome)

        await(connector.deleteOtherEmploymentIncome(deleteRequest)) shouldBe outcome
      }

      "the downstream call is successful and is tax year specific" in new TysIfsTest with Test {
        def taxYear: TaxYear                               = TaxYear.fromMtd("2023-24")
        val deleteRequest: OtherEmploymentIncomeRequest    = OtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/other/employments/23-24/$nino") returns Future.successful(outcome)

        await(connector.deleteOtherEmploymentIncome(deleteRequest)) shouldBe outcome
      }

    }

    "return a 200 result on retrieve" when {
      "the downstream call is successful and is not tax year specific" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

        val retrieveRequest: OtherEmploymentIncomeRequest = OtherEmploymentIncomeRequest(
          nino = Nino(nino),
          taxYear = TaxYear.fromMtd("2021-22")
        )

        val outcome: Right[Nothing, ResponseWrapper[RetrieveOtherEmploymentResponse]] = Right(ResponseWrapper(correlationId, retrieveResponse))

        willGet(s"$baseUrl/income-tax/income/other/employments/$nino/2021-22") returns Future.successful(outcome)

        await(connector.retrieveOtherEmploymentIncome(retrieveRequest)) shouldBe outcome
      }

      "the downstream call is successful and is tax year specific" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val retrieveRequest: OtherEmploymentIncomeRequest                             = OtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveOtherEmploymentResponse]] = Right(ResponseWrapper(correlationId, retrieveResponse))

        willGet(s"$baseUrl/income-tax/income/other/employments/23-24/$nino") returns Future.successful(outcome)

        await(connector.retrieveOtherEmploymentIncome(retrieveRequest)) shouldBe outcome
      }

    }
  }

}
