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

package v1.connectors

import api.connectors.ConnectorSpec
import api.mocks.MockHttpClient
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{NinoFormatError, StandardDownstreamError}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import v1.models.request.deleteCgtPpdOverrides.DeleteCgtPpdOverridesRequest

import scala.concurrent.Future

class DeleteCgtPpdOverridesConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  trait Test extends MockHttpClient with MockAppConfig {

    def taxYear: TaxYear

    val connector: DeleteCgtPpdOverridesConnector = new DeleteCgtPpdOverridesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val request: DeleteCgtPpdOverridesRequest = DeleteCgtPpdOverridesRequest(Nino(nino), taxYear)

  }

  "Delete" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/income/disposals/residential-property/ppd/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(expected))

        await(connector.deleteCgtPpdOverrides(request)) shouldBe expected
      }

      "downstream returns a single error" in new DesTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, NinoFormatError))

        willDelete(
          url = s"$baseUrl/income-tax/income/disposals/residential-property/ppd/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(expected))

        await(connector.deleteCgtPpdOverrides(request)) shouldBe expected
      }

      "downstream returns multiple errors" in new DesTest with Test {

        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val expected = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, StandardDownstreamError)))

        willDelete(
          url = s"$baseUrl/income-tax/income/disposals/residential-property/ppd/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(expected))

        await(connector.deleteCgtPpdOverrides(request)) shouldBe expected
      }

    }
    "return the expected response for a TYS request" when {
      "a valid request is made" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val expected = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/income/disposals/residential-property/ppd/${taxYear.asTysDownstream}/$nino"
        ).returns(Future.successful(expected))

        await(connector.deleteCgtPpdOverrides(request)) shouldBe expected
      }
    }
  }

}
