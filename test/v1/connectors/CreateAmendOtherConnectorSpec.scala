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
import v1.fixtures.other.CreateAmendOtherFixtures.requestBodyModel
import v1.models.request.createAmendOther.CreateAmendOtherRequest

import scala.concurrent.Future

class CreateAmendOtherConnectorSpec extends ConnectorSpec {

  trait Test { _: ConnectorTest =>

    val taxYear: String

    val connector: CreateAmendOtherConnector = new CreateAmendOtherConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    lazy val createAmendOtherRequest: CreateAmendOtherRequest = CreateAmendOtherRequest(
      nino = Nino("AA111111A"),
      taxYear = TaxYear.fromMtd(taxYear),
      body = requestBodyModel
    )

  }

  "CreateAmendOtherConnector" when {
    "createAmend" must {
      "return a 204 status for a success scenario" in new IfsTest with Test {
        val taxYear = "2019-20"
        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/income/other/AA111111A/2019-20",
          body = requestBodyModel
        )
          .returns(Future.successful(outcome))

        await(connector.createAmend(createAmendOtherRequest)) shouldBe outcome
      }

      "return a 204 status for a success scenario for a Tax Year Specific (TYS) tax year" in new TysIfsTest with Test {
        val taxYear = "2023-24"
        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/income/other/23-24/AA111111A",
          body = requestBodyModel
        )
          .returns(Future.successful(outcome))

        await(connector.createAmend(createAmendOtherRequest)) shouldBe outcome
      }
    }
  }

}
