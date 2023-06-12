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

package v2.connectors

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v2.models.request.createAmendDividends.{CreateAmendDividendsRequest, CreateAmendDividendsRequestBody}

import scala.concurrent.Future

class CreateAmendDividendsConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA111111A"

  private val createAmendDividendsRequestBody: CreateAmendDividendsRequestBody = CreateAmendDividendsRequestBody(None, None, None, None, None, None)

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    val createAmendDividendsRequest: CreateAmendDividendsRequest = CreateAmendDividendsRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = createAmendDividendsRequestBody
    )

    val connector: CreateAmendDividendsConnector = new CreateAmendDividendsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
  }

  "CreateAmendDividendsConnector" when {
    "createAmendDividends" must {
      "work for a success scenario" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        willPut(
          url = s"$baseUrl/income-tax/income/dividends/$nino/2019-20",
          body = createAmendDividendsRequestBody
        ) returns Future.successful(outcome)

        await(connector.createAmendDividends(createAmendDividendsRequest)) shouldBe outcome
      }

      "work for a success scenario (TYS)" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        willPut(
          url = s"$baseUrl/income-tax/income/dividends/23-24/$nino",
          body = createAmendDividendsRequestBody
        ) returns Future.successful(outcome)

        await(connector.createAmendDividends(createAmendDividendsRequest)) shouldBe outcome
      }
    }
  }

}
