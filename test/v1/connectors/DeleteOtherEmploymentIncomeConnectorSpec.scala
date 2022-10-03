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

import scala.concurrent.Future

class DeleteOtherEmploymentIncomeConnectorSpec extends ConnectorSpec {

  val nino: String              = "AA123456A"
  val taxYearMtd: String        = "2017-18"
  val taxYearTys: String        = "2023-2024"
  val taxYearDownstream: String = "2018"

  trait Test {
    _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: DeleteOtherEmploymentIncomeConnector =
      new DeleteOtherEmploymentIncomeConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

    protected val request: DeleteOtherEmploymentIncomeRequest =
      DeleteOtherEmploymentIncomeRequest(
        nino = Nino(nino),
        taxYear = taxYear
      )

  }

  "DeleteOtherEmploymentIncomeConnector" should {
    "return a 200 result" when {
      "the downstream call is successful and not tax year specific" in new DesTest with Test {
        def taxYear          = TaxYear.fromMtd(taxYearMtd)
        override val request = DeleteOtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome          = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/other/employments/${request.nino}/${request.taxYear.asDownstream}") returns Future.successful(outcome)

        await(connector.deleteOtherEmploymentIncome(request)) shouldBe outcome
      }

      "the downstream call is successful and is tax year specific" in new TysIfsTest with Test {
        def taxYear          = TaxYear.fromMtd("2023-24")
        override val request = DeleteOtherEmploymentIncomeRequest(Nino(nino), taxYear)
        val outcome          = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/other/employments/2324/${request.nino}") returns Future.successful(
          outcome)

        await(connector.deleteOtherEmploymentIncome(request)) shouldBe outcome
      }

    }
  }

}
