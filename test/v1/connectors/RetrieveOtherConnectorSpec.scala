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
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler
import v1.models.request.retrieveOther.RetrieveOtherRequest
import v1.models.response.retrieveOther.RetrieveOtherResponse

import scala.concurrent.Future

class RetrieveOtherConnectorSpec extends ConnectorSpec {

  "RetrieveOtherConnector" should {

    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new DesTest with Test {
        val outcome = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveOtherResponse] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        override def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, response))

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[RetrieveOtherResponse] = await(connector.retrieve(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val nino: String = "AA111111A"
    def taxYear: TaxYear       = TaxYear.fromMtd("2019-20")

    val request: RetrieveOtherRequest =
      RetrieveOtherRequest(Nino(nino), taxYear)

    val response: RetrieveOtherResponse = RetrieveOtherResponse(
      submittedOn = Timestamp("2019-04-04T01:01:01.000Z"),
      postCessationReceipts = None,
      businessReceipts = None,
      allOtherIncomeReceivedWhilstAbroad = None,
      overseasIncomeAndGains = None,
      chargeableForeignBenefitsAndGifts = None,
      omittedForeignIncome = None
    )

    val connector: RetrieveOtherConnector =
      new RetrieveOtherConnector(http = mockHttpClient, appConfig = mockAppConfig)

    protected def stubHttpResponse(
        outcome: DownstreamOutcome[RetrieveOtherResponse]): CallHandler[Future[DownstreamOutcome[RetrieveOtherResponse]]]#Derived = {
      willGet(
        url = s"$baseUrl/income-tax/income/other/$nino/${taxYear.asMtd}"
      ).returns(Future.successful(outcome))
    }

    protected def stubTysHttpResponse(
        outcome: DownstreamOutcome[RetrieveOtherResponse]): CallHandler[Future[DownstreamOutcome[RetrieveOtherResponse]]]#Derived = {
      willGet(
        url = s"$baseUrl/income-tax/income/other/${taxYear.asTysDownstream}/$nino"
      ).returns(Future.successful(outcome))
    }

  }

}
