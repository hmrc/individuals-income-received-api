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
import v1.fixtures.RetrieveDividendsFixtures.responseModel
import v1.models.request.retrieveDividends.RetrieveDividendsRequest

import scala.concurrent.Future

class RetrieveDividendsConnectorSpec extends ConnectorSpec {

  "RetrieveDividendsConnector" should {

    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        val taxYear = "2019-20"

        willGet(url = s"$baseUrl/income-tax/income/dividends/AA111111A/2019-20").returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }

    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        val taxYear = "2023-24"

        willGet(url = s"$baseUrl/income-tax/income/dividends/23-24/AA111111A").returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>

    val taxYear: String

    val connector: RetrieveDividendsConnector = new RetrieveDividendsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    lazy val request: RetrieveDividendsRequest = RetrieveDividendsRequest(Nino("AA111111A"), TaxYear.fromMtd(taxYear))

    val outcome = Right(ResponseWrapper(correlationId, responseModel))

  }

}
