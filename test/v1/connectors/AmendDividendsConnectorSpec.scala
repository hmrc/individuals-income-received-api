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
import v1.models.request.amendDividends._

import scala.concurrent.Future

class AmendDividendsConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA111111A"

  private val amendDividendsRequestBody: AmendDividendsRequestBody = AmendDividendsRequestBody(None, None, None, None, None, None)

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    val amendDividendsRequest: AmendDividendsRequest = AmendDividendsRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = amendDividendsRequestBody
    )

    val connector: AmendDividendsConnector = new AmendDividendsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val outcome = Right(ResponseWrapper(correlationId, ()))
  }

  "AmendDividendsConnector" when {
    "amendDividends" must {
      "work for a success scenario" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        willPut(
          url = s"$baseUrl/income-tax/income/dividends/$nino/2019-20",
          body = amendDividendsRequestBody
        ) returns Future.successful(outcome)

        await(connector.amendDividends(amendDividendsRequest)) shouldBe outcome
      }

      "work for a success scenario (TYS)" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        willPut(
          url = s"$baseUrl/income-tax/income/dividends/23-24/$nino",
          body = amendDividendsRequestBody
        ) returns Future.successful(outcome)

        await(connector.amendDividends(amendDividendsRequest)) shouldBe outcome
      }
    }
  }

}
