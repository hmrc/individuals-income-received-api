/*
 * Copyright 2021 HM Revenue & Customs
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

package v1r7.services

import v1r7.models.domain.Nino
import v1r7.controllers.EndpointLogContext
import v1r7.mocks.connectors.MockAmendInsurancePoliciesConnector
import v1r7.models.errors._
import v1r7.models.outcomes.ResponseWrapper
import v1r7.models.request.amendInsurancePolicies._

import scala.concurrent.Future

class AmendInsurancePoliciesServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019-20"

  private val voidedIsaModel = AmendVoidedIsaPoliciesItem(
    customerReference = Some("INPOLY123A"),
    event = Some("Death of spouse"),
    gainAmount = 2000.99,
    taxPaidAmount = Some(5000.99),
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12)
  )

  private val lifeInsuranceModel = AmendCommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123A"),
    event = Some("Death of spouse"),
    gainAmount = 2000.99,
    taxPaid = true,
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12),
    deficiencyRelief = Some(5000.99)
  )

  private val lifeAnnuityModel = AmendCommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123A"),
    event = Some("Death of spouse"),
    gainAmount = 2000.99,
    taxPaid = true,
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12),
    deficiencyRelief = Some(5000.99)
  )

  private val foreignModel = AmendForeignPoliciesItem(
    customerReference = Some("INPOLY123A"),
    gainAmount = 2000.99,
    taxPaidAmount = Some(5000.99),
    yearsHeld = Some(15)
  )

  private val capitalRedemptionModel = AmendCommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123A"),
    event = Some("Death of spouse"),
    gainAmount = 2000.99,
    taxPaid = true,
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12),
    deficiencyRelief = Some(5000.99)
  )

  private val amendInsurancePoliciesRequestBody = AmendInsurancePoliciesRequestBody(
    lifeInsurance = Some(Seq(lifeInsuranceModel)),
    capitalRedemption = Some(Seq(capitalRedemptionModel)),
    lifeAnnuity = Some(Seq(lifeAnnuityModel)),
    voidedIsa = Some(Seq(voidedIsaModel)),
    foreign = Some(Seq(foreignModel))
  )

  private val amendInsurancePoliciesRequest = AmendInsurancePoliciesRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = amendInsurancePoliciesRequestBody
  )

  trait Test extends MockAmendInsurancePoliciesConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AmendInsurancePoliciesService = new AmendInsurancePoliciesService(
      connector = mockAmendInsurancePoliciesConnector
    )
  }

  "AmendInsurancePoliciesService" when {
    "amendInsurancePolicies" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendInsurancePoliciesConnector.amendInsurancePolicies(amendInsurancePoliciesRequest)
          .returns(Future.successful(outcome))

        await(service.amendInsurancePolicies(amendInsurancePoliciesRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAmendInsurancePoliciesConnector.amendInsurancePolicies(amendInsurancePoliciesRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amendInsurancePolicies(amendInsurancePoliciesRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", DownstreamError),
          ("INVALID_PAYLOAD",  DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}