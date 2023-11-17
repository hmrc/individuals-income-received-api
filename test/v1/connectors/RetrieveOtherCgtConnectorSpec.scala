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
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveOtherCgt.RetrieveOtherCgtRequest
import v1.models.response.retrieveOtherCgt.RetrieveOtherCgtResponse

import scala.concurrent.Future

class RetrieveOtherCgtConnectorSpec extends ConnectorSpec {

  "RetrieveOtherCgtConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new Api1661Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = s"$baseUrl/income-tax/income/disposals/other-gains/$nino/2019-20"
        ).returns(Future.successful(outcome))

        await(connector.retrieveOtherCgt(request)) shouldBe outcome
      }
    }
    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = s"$baseUrl/income-tax/income/disposals/other-gains/23-24/$nino"
        ).returns(Future.successful(outcome))

        await(connector.retrieveOtherCgt(request)) shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val nino: String = "AA111111A"

    protected val request: RetrieveOtherCgtRequest =
      RetrieveOtherCgtRequest(
        nino = Nino(nino),
        taxYear = taxYear
      )

    val response: RetrieveOtherCgtResponse = RetrieveOtherCgtResponse(
      submittedOn = Timestamp("2021-05-07T16:18:44.403Z"),
      disposals = None,
      nonStandardGains = None,
      losses = None,
      adjustments = None
    )

    val connector: RetrieveOtherCgtConnector = new RetrieveOtherCgtConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

}
