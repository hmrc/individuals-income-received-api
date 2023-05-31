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

package v1andv2.connectors

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import v1andv2.models.request.retrieveForeign.RetrieveForeignRequest
import v1andv2.models.response.retrieveForeign.RetrieveForeignResponse

import scala.concurrent.Future

class RetrieveForeignConnectorSpec extends ConnectorSpec {

  "RetrieveForeignConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignResponse] = await(connector.retrieveForeign(request))
        result shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, response))

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveForeignResponse] = await(connector.retrieveForeign(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val nino: String = "AA111111A"
    def taxYear: TaxYear

    val request: RetrieveForeignRequest =
      RetrieveForeignRequest(Nino(nino), taxYear)

    val response: RetrieveForeignResponse = RetrieveForeignResponse(
      submittedOn = "",
      foreignEarnings = None,
      unremittableForeignIncome = None
    )

    val connector: RetrieveForeignConnector =
      new RetrieveForeignConnector(http = mockHttpClient, appConfig = mockAppConfig)

    protected def stubHttpResponse(
        outcome: DownstreamOutcome[RetrieveForeignResponse]): CallHandler[Future[DownstreamOutcome[RetrieveForeignResponse]]]#Derived = {
      willGet(
        url = s"$baseUrl/income-tax/income/foreign/$nino/${taxYear.asMtd}"
      ).returns(Future.successful(outcome))
    }

    protected def stubTysHttpResponse(
        outcome: DownstreamOutcome[RetrieveForeignResponse]): CallHandler[Future[DownstreamOutcome[RetrieveForeignResponse]]]#Derived = {
      willGet(
        url = s"$baseUrl/income-tax/foreign-income/${taxYear.asTysDownstream}/$nino"
      ).returns(Future.successful(outcome))
    }

  }

}
