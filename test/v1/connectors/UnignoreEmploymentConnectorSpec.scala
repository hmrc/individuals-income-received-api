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

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.Future

class UnignoreEmploymentConnectorSpec extends ConnectorSpec {

  "UnignoreEmploymentConnector" should {
    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        def taxYear: TaxYear                                       = TaxYear.fromMtd("2023-24")
        val expectedOutcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/23-24/employments/$nino/ignore/$employmentId"
        ).returns(Future.successful(expectedOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.unignoreEmployment(request))
        result shouldBe expectedOutcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    val nino: String         = "AA111111A"
    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val request: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      employmentId = employmentId
    )

    val connector: UnignoreEmploymentConnector = new UnignoreEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

}
