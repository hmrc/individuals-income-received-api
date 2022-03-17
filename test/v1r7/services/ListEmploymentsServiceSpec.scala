/*
 * Copyright 2022 HM Revenue & Customs
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

import api.controllers.EndpointLogContext
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, ErrorWrapper, MtdError, NinoFormatError, NotFoundError, StandardDownstreamError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1r7.mocks.connectors.MockListEmploymentsConnector
import v1r7.models.request.listEmployments.ListEmploymentsRequest
import v1r7.models.response.listEmployment.{Employment, ListEmploymentResponse}

import scala.concurrent.Future

class ListEmploymentsServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019-20"

  private val requestData = ListEmploymentsRequest(Nino(nino), taxYear)

  private val validResponse = ListEmploymentResponse(
    employments = Some(Seq(Employment(employmentId = "00000000-0000-1000-8000-000000000000",
      employerName = "Vera Lynn",
      dateIgnored = Some("2020-06-17T10:53:38Z")),
      Employment(employmentId = "00000000-0000-1000-8000-000000000001",
        employerName = "Vera Lynn",
        dateIgnored = Some("2020-06-17T10:53:38Z")))),
    customEmployments =
      Some(Seq(Employment(employmentId = "00000000-0000-1000-8000-000000000002",
        employerName = "Vera Lynn"),
        Employment(employmentId = "00000000-0000-1000-8000-000000000003",
          employerName = "Vera Lynn")))
  )

  trait Test extends MockListEmploymentsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: ListEmploymentsService = new ListEmploymentsService(connector = mockListEmploymentsConnector)
  }

  "ListEmploymentsService" when {
    "listEmployments" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, validResponse))


        MockListEmploymentsConnector.listEmployments(requestData)
          .returns(Future.successful(outcome))

        await(service.listEmployments(requestData)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(ifsErrorCode: String, error: MtdError): Unit =
          s"a $ifsErrorCode error is returned from the service" in new Test {

            MockListEmploymentsConnector.listEmployments(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

            await(service.listEmployments(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", StandardDownstreamError),
          ("INVALID_CORRELATIONID", StandardDownstreamError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
