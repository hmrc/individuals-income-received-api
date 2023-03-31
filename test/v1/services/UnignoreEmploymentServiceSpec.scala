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
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.connectors.MockUnignoreEmploymentConnector
import v1.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.Future

class UnignoreEmploymentServiceSpec extends ServiceSpec {

  "UnignoreEmploymentService" when {
    "unignoreEmployment" should {
      "return correct result for a success" in new Test {
        val expectedOutcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        MockUnignoreEmploymentConnector
          .unignoreEmployment(request)
          .returns(Future.successful(expectedOutcome))

        val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.unignoreEmployment(request))

        result shouldBe expectedOutcome
      }

      "map errors according to spec" should {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"return ${error.code} error when $downstreamErrorCode error is returned from the connector" in new Test {
            val expectedOutcome: Left[ErrorWrapper, Nothing] = Left(ErrorWrapper(correlationId, error))

            MockUnignoreEmploymentConnector
              .unignoreEmployment(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.unignoreEmployment(request))

            result shouldBe expectedOutcome
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", EmploymentIdFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("CUSTOMER_ADDED", RuleCustomEmploymentUnignoreError),
          ("NO_DATA_FOUND", NotFoundError),
          ("BEFORE_TAX_YEAR_ENDED", RuleTaxYearNotEndedError),
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

  trait Test extends MockUnignoreEmploymentConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val nino         = "AA112233A"
    val taxYear      = "2021-22"
    val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val request: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
      nino = Nino(nino),
      taxYear = TaxYear.fromMtd(taxYear),
      employmentId = employmentId
    )

    val service: UnignoreEmploymentService = new UnignoreEmploymentService(
      connector = mockUnignoreEmploymentConnector
    )

  }

}
