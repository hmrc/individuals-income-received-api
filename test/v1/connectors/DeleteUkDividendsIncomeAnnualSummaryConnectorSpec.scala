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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsObject
import v1.models.request.deleteUkDividendsIncomeAnnualSummary.DeleteUkDividendsIncomeAnnualSummaryRequest

import scala.concurrent.Future

class DeleteUkDividendsIncomeAnnualSummaryConnectorSpec extends ConnectorSpec {

  val nino: String              = "AA123456A"

  "DeleteUkDividendsIncomeAnnualSummaryConnector" should {
    "delete called" when {
      "return a 200 status for a success scenario" in {
        new DesTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

          val outcome = Right(ResponseWrapper(correlationId, ()))

          willPost(s"$baseUrl/income-tax/nino/$nino/income-source/dividends/annual/${taxYear.asDownstream}", JsObject.empty) returns Future.successful(outcome)

          val result: DownstreamOutcome[Unit] = await(connector.delete(request))
          result shouldBe outcome
        }
      }
    }

    "delete called for a Tax Year Specific tax year" when {
      "return a 200 status for a success scenario" in {
        new TysIfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val outcome = Right(ResponseWrapper(correlationId, ()))

          willPost(s"$baseUrl/income-tax/${taxYear.asTysDownstream}/$nino/income-source/dividends/annual", JsObject.empty) returns Future.successful(outcome)

          val result: DownstreamOutcome[Unit] = await(connector.delete(request))
          result shouldBe outcome
        }
      }
    }
  }

  trait Test { _: ConnectorTest =>
    
    def taxYear: TaxYear

    protected val connector: DeleteUkDividendsIncomeAnnualSummaryConnector = new DeleteUkDividendsIncomeAnnualSummaryConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request: DeleteUkDividendsIncomeAnnualSummaryRequest = DeleteUkDividendsIncomeAnnualSummaryRequest(
      nino = Nino(nino),
      taxYear = taxYear
    )
  }

}
