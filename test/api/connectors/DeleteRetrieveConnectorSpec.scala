/*
 * Copyright 2022 HM Revenue & Customs
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

package api.connectors

import api.connectors.DownstreamUri.{DesUri, TaxYearSpecificIfsUri}
import api.mocks.MockHttpClient
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.{Json, Reads}

import scala.concurrent.Future

class DeleteRetrieveConnectorSpec extends ConnectorSpec {

  val nino: String    = "AA111111A"
  val taxYear: String = "2019-20"

  class Test extends MockHttpClient with MockAppConfig {
    case class Data(field: String)

    object Data {
      implicit val reads: Reads[Data] = Json.reads[Data]
    }

    val connector: DeleteRetrieveConnector = new DeleteRetrieveConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  "DeleteRetrieveConnector" when {
    "delete" must {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      "return a 204 status for a success scenario for a DesUri" in new Test with DesTest {
        val desUri: DesUri[Unit] = DesUri[Unit](s"income-tax/income/savings/$nino/$taxYear")

        willDelete(s"$baseUrl/income-tax/income/savings/$nino/$taxYear")
          .returns(Future.successful(outcome))

        await(connector.delete(desUri)) shouldBe outcome
      }

      "return a 204 status for a success scenario for a TaxYearSpecificIfsUri" in new Test with TysIfsTest {
        val tysUri: TaxYearSpecificIfsUri[Unit] = TaxYearSpecificIfsUri[Unit](s"income-tax/income/savings/$nino/$taxYear")

        willDelete(s"$baseUrl/income-tax/income/savings/$nino/$taxYear")
          .returns(Future.successful(outcome))

        await(connector.delete(tysUri)) shouldBe outcome
      }
    }

    "retrieve" must {

      "return a 200 status for a success scenario for a DesUri" in new Test with DesTest {
        val outcome              = Right(ResponseWrapper(correlationId, Data("value")))
        val desUri: DesUri[Data] = DesUri[Data](s"income-tax/income/savings/$nino/$taxYear")

        willGet(s"$baseUrl/income-tax/income/savings/$nino/$taxYear")
          .returns(Future.successful(outcome))

        await(connector.retrieve[Data](desUri)) shouldBe outcome
      }

      "return a 200 status for a success scenario for a TaxYearSpecificIfsUri" in new Test with TysIfsTest {
        val outcome                             = Right(ResponseWrapper(correlationId, Data("value")))
        val tysUri: TaxYearSpecificIfsUri[Data] = TaxYearSpecificIfsUri[Data](s"income-tax/income/savings/$nino/$taxYear")

        willGet(s"$baseUrl/income-tax/income/savings/$nino/$taxYear")
          .returns(Future.successful(outcome))

        await(connector.retrieve[Data](tysUri)) shouldBe outcome
      }
    }
  }

}
