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
import api.models.domain.{MtdSourceEnum, Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.fixtures.RetrieveNonPayeEmploymentControllerFixture._
import v1.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRequest

import scala.concurrent.Future

class RetrieveNonPayeEmploymentConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"

  "RetrieveNonPayeEmploymentConnectorSpec" when {
    "retrieveUKDividendsIncomeAnnualSummary is called" must {
      "return a 200 for success scenario" in new Api1661Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

        val outcome = Right(ResponseWrapper(correlationId, responseModel))

        willGet(s"$baseUrl/income-tax/income/employments/non-paye/$nino/2018-19?view=LATEST")
          .returns(Future.successful(outcome))

        await(connector.retrieveNonPayeEmployment(request)) shouldBe outcome

      }
    }

    "retrieveUkDividendsIncomeAnnualSummary is called for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, responseModel))

        willGet(s"$baseUrl/income-tax/income/employments/non-paye/23-24/$nino?view=LATEST")
          .returns(Future.successful(outcome))

        await(connector.retrieveNonPayeEmployment(request)) shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: RetrieveNonPayeEmploymentConnector =
      new RetrieveNonPayeEmploymentConnector(http = mockHttpClient, appConfig = mockAppConfig)

    protected val request: RetrieveNonPayeEmploymentIncomeRequest =
      RetrieveNonPayeEmploymentIncomeRequest(Nino("AA111111A"), taxYear = taxYear, MtdSourceEnum.latest)

  }

}
