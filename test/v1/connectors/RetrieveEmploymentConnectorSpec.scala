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
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveEmployment.RetrieveEmploymentRequest
import v1.models.response.retrieveEmployment.RetrieveEmploymentResponse

import scala.concurrent.Future

class RetrieveEmploymentConnectorSpec extends ConnectorSpec {

  private val nino: String    = "AA111111A"
  private val taxYear: String = "2019-20"
  private val employmentId    = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  "RetrieveEmploymentConnector" should {
    "return a 200 status and expected response for a success scenario" in new Release6Test with Test {

      willGet(url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear?employmentId=$employmentId").returns(Future.successful(outcome))

      await(connector.retrieve(request)) shouldBe outcome
    }
  }

  trait Test { _: ConnectorTest =>

    val connector: RetrieveEmploymentConnector = new RetrieveEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val request: RetrieveEmploymentRequest = RetrieveEmploymentRequest(Nino(nino), taxYear, employmentId)

    val responseModel: RetrieveEmploymentResponse = RetrieveEmploymentResponse(
      employerRef = Some("123/AB56797"),
      employerName = "Employer Name Ltd.",
      startDate = Some("2020-06-17"),
      cessationDate = Some("2020-06-17"),
      payrollId = Some("123345657"),
      occupationalPension = Some(false),
      dateIgnored = None,
      submittedOn = None
    )

    val outcome: Right[Nothing, ResponseWrapper[RetrieveEmploymentResponse]] = Right(ResponseWrapper(correlationId, responseModel))
  }

}
