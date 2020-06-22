/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.services

import uk.gov.hmrc.domain.Nino
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockAmendInsurancePoliciesConnector
import v1.models.domain.DesTaxYear
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendInsurancePolicies.{AmendCommonInsurancePoliciesItem, AmendForeignPoliciesItem, AmendInsurancePoliciesRequestBody, AmendInsurancePoliciesRequest, AmendVoidedIsaPoliciesItem}

import scala.concurrent.Future

class AmendInsurancePoliciesServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019"
  private val correlationId = "X-corr"

  val voidedIsaModel = AmendVoidedIsaPoliciesItem(
    customerReference = "INPOLY123A",
    event = Some("Death of spouse"),
    gainAmount = Some(2000.99),
    taxAmountPaid = Some(5000.99),
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12)
  )

  val lifeInsuranceModel = AmendCommonInsurancePoliciesItem(
    customerReference = "INPOLY123A",
    event = Some("Death of spouse"),
    gainAmount = Some(2000.99),
    taxPaid = true,
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12),
    deficiencyRelief = Some(5000.99)
  )

  val lifeAnnuityModel = AmendCommonInsurancePoliciesItem(
    customerReference = "INPOLY123A",
    event = Some("Death of spouse"),
    gainAmount = Some(2000.99),
    taxPaid = true,
    yearsHeld = Some(15),
    yearsHeldSinceLastGain = Some(12),
    deficiencyRelief = Some(5000.99)
  )

  val foreignModel = AmendForeignPoliciesItem(
    customerReference = "INPOLY123A",
    gainAmount = Some(2000.99),
    taxAmountPaid = Some(5000.99),
    yearsHeld = Some(15)
  )

  val capitalRedemptionModel = AmendCommonInsurancePoliciesItem(
    customerReference = "INPOLY123A",
    event = Some("Death of spouse"),
    gainAmount = Some(2000.99),
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
    taxYear = DesTaxYear(taxYear),
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

            await(service.amendInsurancePolicies(amendInsurancePoliciesRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
