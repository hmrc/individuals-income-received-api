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
import v1.fixtures.other.CreateAmendOtherCgtConnectorServiceFixture.mtdRequestBody
import v1.models.request.createAmendOtherCgt._

import scala.concurrent.Future

class CreateAmendOtherCgtConnectorSpec extends ConnectorSpec {

  private val nino: String    = "AA111111A"

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear
    val connector: CreateAmendOtherCgtConnector = new CreateAmendOtherCgtConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }

  "createAndAmend" should {
    "return a 204 status" when {
      "a valid request is made" in new Api1661Test with Test {
        override val taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val createAmendOtherCgtRequest = CreateAmendOtherCgtRequest(
          nino = Nino(nino),
          taxYear = taxYear,
          body = mtdRequestBody
        )

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/income/disposals/other-gains/$nino/2019-20",
          body = mtdRequestBody
        ).returns(Future.successful(outcome))

        await(connector.createAndAmend(createAmendOtherCgtRequest)) shouldBe outcome
      }

      "a valid request is made with Tax Year Specific tax year" in new TysIfsTest with Test {

        override val taxYear: TaxYear = TaxYear.fromMtd("2023-24")
        val createAmendOtherCgtRequest = CreateAmendOtherCgtRequest(
          nino = Nino(nino),
          taxYear = taxYear,
          body = mtdRequestBody
        )
        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/income/disposals/other-gains/23-24/$nino",
          body = mtdRequestBody,
        ).returns(Future.successful(outcome))

        await(connector.createAndAmend(createAmendOtherCgtRequest)) shouldBe outcome
      }
    }
  }

}
