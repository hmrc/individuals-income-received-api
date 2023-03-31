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
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.connectors.MockRetrieveEmploymentConnector
import v1.models.request.retrieveEmployment.RetrieveEmploymentRequest
import v1.models.response.retrieveEmployment.RetrieveEmploymentResponse

import scala.concurrent.Future

class RetrieveEmploymentServiceSpec extends ServiceSpec {

  "RetrieveEmploymentService" should {
    "return correct result for a success" when {
      "a valid request is made" in new Test {
        val outcome: Right[Nothing, ResponseWrapper[RetrieveEmploymentResponse]] = Right(ResponseWrapper(correlationId, responseModel))

        MockRetrieveEmploymentConnector
          .retrieve(request)
          .returns(Future.successful(outcome))

        await(service.retrieve(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveEmploymentConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieve(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockRetrieveEmploymentConnector {

    private val nino    = "AA112233A"
    private val taxYear = "2019-20"
    val employmentId    = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val request: RetrieveEmploymentRequest = RetrieveEmploymentRequest(Nino(nino), taxYear, employmentId)

    val responseModel: RetrieveEmploymentResponse = RetrieveEmploymentResponse(
      employerRef = Some("123/AB56797"),
      employerName = "Employer Name Ltd.",
      startDate = Some("2020-06-17"),
      cessationDate = Some("2020-06-17"),
      payrollId = Some("123345657"),
      occupationalPension = Some(false),
      dateIgnored = None,
      submittedOn = None
    )

    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "RetrieveEmployment")

    val service: RetrieveEmploymentService = new RetrieveEmploymentService(
      connector = mockRetrieveEmploymentConnector
    )

  }

}
