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
import v1.fixtures.insurancePolicies.AmendInsurancePoliciesFixture._
import v1.mocks.connectors.MockAmendInsurancePoliciesConnector
import v1.models.domain.DesTaxYear
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.insurancePolicies.amend.{AmendRequest, AmendRequestBody}

import scala.concurrent.Future

class AmendInsurancePoliciesServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019"
  private val correlationId = "X-corr"

  private val amendInsurancePolsRequest = AmendRequest(
      nino = Nino(nino),
      taxYear = DesTaxYear(taxYear),
      body = AmendRequestBody(
        lifeInsurance = Some(Seq(fullLifeInsuranceModel)),
        capitalRedemption = Some(Seq(fullCapitalRedemptionModel)),
        lifeAnnuity = Some(Seq(fullLifeAnnuityModel)),
        voidedIsa = Some(Seq(fullVoidedIsaModel)),
        foreign = Some(Seq(fullForeignModel)))
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

        MockAmendInsurancePoliciesConnector.amendInsurancePolicies(amendInsurancePolsRequest)
          .returns(Future.successful(outcome))

        await(service.amendInsurancePolicies(amendInsurancePolsRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAmendInsurancePoliciesConnector.amendInsurancePolicies(amendInsurancePolsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amendInsurancePolicies(amendInsurancePolsRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
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
