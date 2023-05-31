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

package v1andv2.connectors

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1andv2.models.request.amendForeign.{AmendForeignRequest, AmendForeignRequestBody, ForeignEarnings, UnremittableForeignIncomeItem}

import scala.concurrent.Future

class AmendForeignConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA111111A"

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector: AmendForeignConnector = new AmendForeignConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    private val foreignEarningsModel = ForeignEarnings(
      customerReference = Some("ref"),
      earningsNotTaxableUK = 111.11
    )

    private val unremittableForeignIncomeModel = Seq(
      UnremittableForeignIncomeItem(
        countryCode = "DEU",
        amountInForeignCurrency = 222.22,
        amountTaxPaid = Some(333.33)
      ),
      UnremittableForeignIncomeItem(
        countryCode = "FRA",
        amountInForeignCurrency = 444.44,
        amountTaxPaid = Some(555.55)
      )
    )

    protected val amendForeignRequestBody = AmendForeignRequestBody(
      foreignEarnings = Some(foreignEarningsModel),
      unremittableForeignIncome = Some(unremittableForeignIncomeModel)
    )

    protected val amendForeignRequest: AmendForeignRequest = AmendForeignRequest(nino = Nino(nino), taxYear = taxYear, body = amendForeignRequestBody)

  }

  "amendForeign" should {
    "return the expected response for a non-TYS request" when {
      "a valid request is made" in new IfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/income/foreign/$nino/${taxYear.asMtd}",
          body = amendForeignRequestBody
        ).returns(Future.successful(outcome))

        await(connector.amendForeign(amendForeignRequest)) shouldBe outcome
      }

      "return the expected response for a TYS request" when {
        "a valid request is made" in new TysIfsTest with Test {
          def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

          val outcome = Right(ResponseWrapper(correlationId, ()))

          willPut(
            url = s"$baseUrl/income-tax/foreign-income/${taxYear.asTysDownstream}/$nino",
            body = amendForeignRequestBody
          ).returns(Future.successful(outcome))

          await(connector.amendForeign(amendForeignRequest)) shouldBe outcome
        }
      }
    }
  }

}
