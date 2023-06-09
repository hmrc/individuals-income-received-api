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

package v1.services

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.mocks.connectors.MockRetrieveInsurancePoliciesConnector
import v1.models.request.retrieveInsurancePolicies.RetrieveInsurancePoliciesRequest
import v1.models.response.retrieveInsurancePolicies.RetrieveInsurancePoliciesResponse

import scala.concurrent.Future

class RetrieveInsurancePoliciesServiceSpec extends ServiceSpec {

  "RetrieveInsurancePoliciesServiceSpec" should {
    "retrieveInsurancePolicies" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, response))

        MockRetrieveInsurancePoliciesConnector
          .retrieve(request)
          .returns(Future.successful(outcome))

        await(service.retrieve(request)) shouldBe outcome
      }

      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveInsurancePoliciesConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieve(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = List(
          ("INVALID_CORRELATION_ID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockRetrieveInsurancePoliciesConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    private val nino    = Nino("AA112233A")
    private val taxYear = TaxYear.fromMtd("2019-20")

    val request: RetrieveInsurancePoliciesRequest = RetrieveInsurancePoliciesRequest(
      nino = nino,
      taxYear = taxYear
    )

    val response: RetrieveInsurancePoliciesResponse = RetrieveInsurancePoliciesResponse(
      submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
      lifeInsurance = None,
      capitalRedemption = None,
      lifeAnnuity = None,
      voidedIsa = None,
      foreign = None
    )

    val service: RetrieveInsurancePoliciesService = new RetrieveInsurancePoliciesService(
      connector = mockRetrieveInsurancePoliciesConnector
    )

  }

}
