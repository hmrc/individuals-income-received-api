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
import v1.fixtures.nonPayeEmployment.CreateAmendNonPayeEmploymentServiceConnectorFixture._
import v1.models.request.createAmendNonPayeEmployment._

import scala.concurrent.Future

class CreateAmendNonPayeEmploymentConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA111111A"

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    def request = CreateAmendNonPayeEmploymentRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = requestBodyModel
    )

    val connector: CreateAmendNonPayeEmploymentConnector = new CreateAmendNonPayeEmploymentConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  "createAndAmend" should {
    "return a 204 status" when {
      "a valid request is made" in new Api1661Test with Test {
        lazy val taxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/income/employments/non-paye/$nino/2019-20",
          body = requestBodyModel
        )
          .returns(Future.successful(outcome))

        await(connector.createAndAmend(request)) shouldBe outcome
      }

      "a valid request is made for a TYS tax year" in new TysIfsTest with Test {
        val taxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/income/employments/non-paye/23-24/$nino",
          body = requestBodyModel
        )
          .returns(Future.successful(outcome))

        await(connector.createAndAmend(request)) shouldBe outcome
      }
    }
  }

}
