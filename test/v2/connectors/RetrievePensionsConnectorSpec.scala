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
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.outcomes.ResponseWrapper
import v2.models.request.retrievePensions.RetrievePensionsRequest
import v2.models.response.retrievePensions.RetrievePensionsResponse

import scala.concurrent.Future

class RetrievePensionsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"

  val retrievePensionsResponse: RetrievePensionsResponse = RetrievePensionsResponse(
    submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
    foreignPensions = None,
    overseasPensionContributions = None
  )

  trait Test {
    _: ConnectorTest =>

    def taxYear: String

    val connector: RetrievePensionsConnector = new RetrievePensionsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    lazy val request: RetrievePensionsRequest = RetrievePensionsRequest(Nino(nino), TaxYear.fromMtd(taxYear))
  }

  "RetrievePensionsConnector" when {
    "retrieve" must {
      "return a 200 status for a success scenario" in new IfsTest with Test {

        def taxYear: String = "2021-22"

        val outcome = Right(ResponseWrapper(correlationId, retrievePensionsResponse))

        willGet(url = s"$baseUrl/income-tax/income/pensions/$nino/2021-22")
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }

      "return a 200 status for a success scenario for a TYS tax year" in new TysIfsTest with Test {

        def taxYear: String = "2023-24"

        val outcome = Right(ResponseWrapper(correlationId, retrievePensionsResponse))

        willGet(url = s"$baseUrl/income-tax/income/pensions/23-24/$nino")
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }

    }

  }

}
