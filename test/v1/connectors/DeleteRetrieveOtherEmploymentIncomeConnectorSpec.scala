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
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.models.request.deleteOtherEmploymentIncome.DeleteOtherEmploymentIncomeRequest
import v1.models.request.retrieveOtherEmploymentIncome.RetrieveOtherEmploymentIncomeRequest
import v1.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse
import v1.fixtures.OtherIncomeEmploymentFixture.retrieveResponse

import scala.concurrent.Future

class DeleteRetrieveOtherEmploymentIncomeConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  trait Test {
    _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: DeleteRetrieveOtherEmploymentIncomeConnector =
      new DeleteRetrieveOtherEmploymentIncomeConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

  }

  "DeleteRetrieveOtherEmploymentIncomeConnector" should {
    "return a 200 result on delete" when {
      "the downstream call is successful and not tax year specific" in new DesTest with Test {
        def taxYear: TaxYear                                  = TaxYear.fromMtd("2017-18")
        val deleteRequest: DeleteOtherEmploymentIncomeRequest = DeleteOtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[Unit]]    = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/other/employments/$nino/2017-18") returns Future.successful(outcome)

        await(connector.deleteOtherEmploymentIncome(deleteRequest)) shouldBe outcome
      }

      "the downstream call is successful and is tax year specific" in new TysIfsTest with Test {
        def taxYear: TaxYear                                  = TaxYear.fromMtd("2023-24")
        val deleteRequest: DeleteOtherEmploymentIncomeRequest = DeleteOtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[Unit]]    = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/other/employments/23-24/$nino") returns Future.successful(outcome)

        await(connector.deleteOtherEmploymentIncome(deleteRequest)) shouldBe outcome
      }

    }

    "return a 200 result on retrieve" when {
      "the downstream call is successful and is not tax year specific" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val retrieveRequest: RetrieveOtherEmploymentIncomeRequest = RetrieveOtherEmploymentIncomeRequest(
          nino = Nino(nino),
          taxYear = TaxYear.fromMtd("2023-24")
        )

        val outcome: Right[Nothing, ResponseWrapper[RetrieveOtherEmploymentResponse]] = Right(ResponseWrapper(correlationId, retrieveResponse))

        willGet(s"$baseUrl/income-tax/income/other/employments/$nino/2023-24") returns Future.successful(outcome)

        await(connector.retrieveOtherEmploymentIncome(retrieveRequest)) shouldBe outcome
      }

      "the downstream call is successful and is tax year specific" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val retrieveRequest: RetrieveOtherEmploymentIncomeRequest                     = RetrieveOtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveOtherEmploymentResponse]] = Right(ResponseWrapper(correlationId, retrieveResponse))

        willGet(s"$baseUrl/income-tax/income/other/employments/23-24/$nino") returns Future.successful(outcome)

        await(connector.retrieveOtherEmploymentIncome(retrieveRequest)) shouldBe outcome
      }

    }
  }

}
