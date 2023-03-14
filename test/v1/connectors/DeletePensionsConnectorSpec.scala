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
import api.models.errors.{NinoFormatError, InternalError}
import api.models.outcomes.ResponseWrapper
import v1.models.request.deletePensions.DeletePensionsRequest

import scala.concurrent.Future

class DeletePensionsConnectorSpec extends ConnectorSpec {

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    val nino: String = "AA111111A"

    protected val request: DeletePensionsRequest =
      DeletePensionsRequest(
        nino = Nino(nino),
        taxYear = taxYear
      )

    protected val connector: DeletePensionsIncomeConnector = new DeletePensionsIncomeConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  "DeletePensionsIncomeConnector" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2021-22")
        val outcome          = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/income/pensions/$nino/${taxYear.asMtd}"
        ).returns(Future.successful(outcome))

        await(connector.deletePensionsIncome(request)) shouldBe outcome
      }
    }
    "downstream returns a single error" in new IfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

      val outcome = Left(ResponseWrapper(correlationId, NinoFormatError))

      willDelete(
        s"$baseUrl/income-tax/income/pensions/$nino/${taxYear.asMtd}"
      ).returns(Future.successful(outcome))

      await(connector.deletePensionsIncome(request)) shouldBe outcome
    }

    "downstream returns multiple errors" in new IfsTest with Test {

      def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

      val outcome = Left(ResponseWrapper(correlationId, Seq(NinoFormatError, InternalError)))

      willDelete(
        s"$baseUrl/income-tax/income/pensions/$nino/${taxYear.asMtd}"
      ).returns(Future.successful(outcome))

      await(connector.deletePensionsIncome(request)) shouldBe outcome
    }

  }

  "return the expected response for a TYS request" when {
    "a valid request is made" in new TysIfsTest with Test {
      def taxYear: TaxYear = TaxYear.fromMtd("2023-24")
      val outcome          = Right(ResponseWrapper(correlationId, ()))

      willDelete(
        url = s"$baseUrl/income-tax/income/pensions/${taxYear.asTysDownstream}/$nino"
      ).returns(Future.successful(outcome))

      await(connector.deletePensionsIncome(request)) shouldBe outcome
    }
  }

}
