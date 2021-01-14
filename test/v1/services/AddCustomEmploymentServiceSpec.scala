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

package v1.services

import uk.gov.hmrc.domain.Nino
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockAddCustomEmploymentConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.addCustomEmployment.{AddCustomEmploymentRequest, AddCustomEmploymentRequestBody}
import v1.models.response.addCustomEmployment.AddCustomEmploymentResponse

import scala.concurrent.Future

class AddCustomEmploymentServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2021-22"

  val addCustomEmploymentRequestBody: AddCustomEmploymentRequestBody = AddCustomEmploymentRequestBody(
    employerRef = Some("123/AB56797"),
    employerName = "BBC infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412")
  )

  val request: AddCustomEmploymentRequest = AddCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = addCustomEmploymentRequestBody
  )

  val response: AddCustomEmploymentResponse = AddCustomEmploymentResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")


  trait Test extends MockAddCustomEmploymentConnector{
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AddCustomEmploymentService = new AddCustomEmploymentService(
      connector = mockAddCustomEmploymentConnector
    )
  }

  "AddCustomEmploymentService" when {
    ".addEmployment" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, response))

        MockAddCustomEmploymentConnector.addEmployment(request)
          .returns(Future.successful(outcome))

        await(service.addEmployment(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAddCustomEmploymentConnector.addEmployment(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.addEmployment(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("NOT_SUPPORTED_TAX_YEAR", RuleTaxYearNotEndedError),
          ("INVALID_DATE_RANGE", RuleStartDateAfterTaxYearEndError),
          ("INVALID_CESSATION_DATE", RuleCessationDateBeforeTaxYearStartError),
          ("INVALID_PAYLOAD", DownstreamError),
          ("INVALID_CORRELATIONID", DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}