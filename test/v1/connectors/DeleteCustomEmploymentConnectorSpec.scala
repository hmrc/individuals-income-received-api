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
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import v1.models.request.deleteCustomEmployment.DeleteCustomEmploymentRequest

import scala.concurrent.Future

class DeleteCustomEmploymentConnectorSpec extends ConnectorSpec {

  private val nino: String    = "AA111111A"
  private val taxYear: String = "2019-20"
  private val employmentId    = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "DeleteCustomEmploymentConnector" should {
    "return a 200 result on delete" when {
      "the downstream call is successful" in new IfsTest with Test {

        willDelete(s"$baseUrl/income-tax/income/employments/$nino/$taxYear/custom/$employmentId") returns Future.successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome

      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    val connector: DeleteCustomEmploymentConnector = new DeleteCustomEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request: DeleteCustomEmploymentRequest =
      DeleteCustomEmploymentRequest(
        nino = Nino(nino),
        taxYear = taxYear,
        employmentId = employmentId
      )

    val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
  }

}
