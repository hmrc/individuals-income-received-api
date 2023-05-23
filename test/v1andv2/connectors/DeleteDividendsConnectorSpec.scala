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
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import v1andv2.models.request.deleteDividends.DeleteDividendsRequest

import scala.concurrent.Future

class DeleteDividendsConnectorSpec extends ConnectorSpec {

  private val nino: String    = "AA123456A"
  private val taxYear: String = "2021-22"

  "DeleteDividendsConnector" should {
    "return a 200 result on delete" when {
      "the downstream call is successful and not tax year specific" in new IfsTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/dividends/$nino/$taxYear") returns Future.successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val connector: DeleteDividendsConnector =
      new DeleteDividendsConnector(
        http = mockHttpClient,
        appConfig = mockAppConfig
      )

    protected val request: DeleteDividendsRequest =
      DeleteDividendsRequest(
        nino = Nino(nino),
        taxYear = taxYear
      )

  }

}
