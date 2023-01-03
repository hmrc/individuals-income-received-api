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
import v1.models.request.amendInsurancePolicies._

import scala.concurrent.Future

class AmendInsurancePoliciesConnectorSpec extends ConnectorSpec {

  trait Test { _: ConnectorTest =>

    val nino: String = "AA111111A"
    val taxYear: TaxYear

    val voidedIsaModel = AmendVoidedIsaPoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12)
    )

    val lifeInsuranceModel = AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )

    val lifeAnnuityModel = AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )

    val foreignModel = AmendForeignPoliciesItem(
      customerReference = Some("INPOLY123A"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15)
    )

    val capitalRedemptionModel = AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )

    val amendInsurancePoliciesBody = AmendInsurancePoliciesRequestBody(
      lifeInsurance = Some(Seq(lifeInsuranceModel)),
      capitalRedemption = Some(Seq(capitalRedemptionModel)),
      lifeAnnuity = Some(Seq(lifeAnnuityModel)),
      voidedIsa = Some(Seq(voidedIsaModel)),
      foreign = Some(Seq(foreignModel))
    )

    def request = AmendInsurancePoliciesRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = amendInsurancePoliciesBody
    )

    val connector: AmendInsurancePoliciesConnector = new AmendInsurancePoliciesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  "AmendInsurancePoliciesConnector" when {
    "amendInsurancePolicies" must {
      "return a 201 status for a success scenario" in new IfsTest with Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))
        val taxYear = TaxYear.fromMtd("2019-20")

        willPut(s"$baseUrl/income-tax/insurance-policies/income/$nino/2019-20", amendInsurancePoliciesBody) returns Future
          .successful(outcome)

        val result = await(connector.amendInsurancePolicies(request))
        result shouldBe outcome

      }
    }

    "amendInsurancePolicies called for a TYS tax year" must {
      "return a 201 status for a success scenario" in new TysIfsTest with Test {
        val taxYear = TaxYear.fromMtd("2023-24")
        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(s"$baseUrl/income-tax/insurance-policies/income/23-24/${nino}", amendInsurancePoliciesBody) returns Future
          .successful(outcome)

        val result = await(connector.amendInsurancePolicies(request))
        result shouldBe outcome
      }
    }
  }

}
