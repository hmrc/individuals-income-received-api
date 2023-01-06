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
import api.models.request.EmptyBody
import v1.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.Future

class IgnoreEmploymentConnectorSpec extends ConnectorSpec {

  val nino: String         = "AA111111A"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    val connector: IgnoreEmploymentConnector = new IgnoreEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val request: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      employmentId = employmentId
    )

    val outcome = Right(ResponseWrapper(correlationId, ()))
  }

  "IgnoreEmploymentConnector" when {
    "ignoreEmployment" should {
      "work" in new Release6Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

        willPut(
          url = s"$baseUrl/income-tax/income/employments/$nino/2021-22/$employmentId/ignore",
          body = EmptyBody
        ) returns Future.successful(outcome)

        await(connector.ignoreEmployment(request)) shouldBe outcome

      }

      "work for TYS" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        willPut(
          url = s"$baseUrl/income-tax/23-24/income/employments/$nino/$employmentId/ignore",
          body = EmptyBody
        ) returns Future.successful(outcome)

        await(connector.ignoreEmployment(request)) shouldBe outcome
      }
    }
  }

}
