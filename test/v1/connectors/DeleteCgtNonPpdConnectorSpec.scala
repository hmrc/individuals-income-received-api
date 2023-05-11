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
import v1.models.request.deleteCgtNonPpd.DeleteCgtNonPpdRequest

import scala.concurrent.Future

class DeleteCgtNonPpdConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"

  "DeleteCgtNonPpdConnectorSpec" when {
    "deleteCgtNonPpd is called" must {
      "return a 200 for success scenario" in new Api1661Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2018-19")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/disposals/residential-property/$nino/2018-19")
          .returns(Future.successful(outcome))

        await(connector.deleteCgtNonPpd(request)) shouldBe outcome

      }
    }

    "deleteCgtNonPpd is called for a TaxYearSpecific tax year" must {
      "return a 200 for success scenario" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/income/disposals/residential-property/23-24/$nino")
          .returns(Future.successful(outcome))

        await(connector.deleteCgtNonPpd(request)) shouldBe outcome
      }
    }
  }

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: DeleteCgtNonPpdConnector = new DeleteCgtNonPpdConnector(http = mockHttpClient, appConfig = mockAppConfig)

    protected val request: DeleteCgtNonPpdRequest = DeleteCgtNonPpdRequest(Nino("AA111111A"), taxYear = taxYear)

  }

}
