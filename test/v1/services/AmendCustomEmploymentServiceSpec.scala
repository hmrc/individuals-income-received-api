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
import v1.mocks.connectors.MockAmendCustomEmploymentConnector
import v1.models.request.amendCustomEmployment.{AmendCustomEmploymentRequest, AmendCustomEmploymentRequestBody}

import scala.concurrent.Future

class AmendCustomEmploymentServiceSpec extends ServiceSpec {

  private val nino         = "AA112233A"
  private val taxYear      = "2021-22"
  private val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val amendCustomEmploymentRequestBody: AmendCustomEmploymentRequestBody = AmendCustomEmploymentRequestBody(
    employerRef = Some("123/AB56797"),
    employerName = "BBC infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412"),
    occupationalPension = false
  )

  val request: AmendCustomEmploymentRequest = AmendCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId,
    body = amendCustomEmploymentRequestBody
  )

  trait Test extends MockAmendCustomEmploymentConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AmendCustomEmploymentService = new AmendCustomEmploymentService(
      connector = mockAmendCustomEmploymentConnector
    )

  }

  "AmendCustomEmploymentService" when {
    ".amendEmployment" should {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendCustomEmploymentConnector
          .amendEmployment(request)
          .returns(Future.successful(outcome))

        await(service.amendEmployment(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAmendCustomEmploymentConnector
              .amendEmployment(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.amendEmployment(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", EmploymentIdFormatError),
          ("NOT_SUPPORTED_TAX_YEAR", RuleTaxYearNotEndedError),
          ("INVALID_DATE_RANGE", RuleStartDateAfterTaxYearEndError),
          ("INVALID_CESSATION_DATE", RuleCessationDateBeforeTaxYearStartError),
          ("CANNOT_UPDATE", RuleUpdateForbiddenError),
          ("NO_DATA_FOUND", NotFoundError),
          ("INVALID_PAYLOAD", InternalError),
          ("INVALID_CORRELATIONID", InternalError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
