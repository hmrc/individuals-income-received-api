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
import v1.mocks.connectors.MockIgnoreEmploymentConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.ignoreEmployment.{IgnoreEmploymentRequest, IgnoreEmploymentRequestBody}

import scala.concurrent.Future

class IgnoreEmploymentServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2021-22"
  private val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val ignoreEmploymentRequestBody: IgnoreEmploymentRequestBody = IgnoreEmploymentRequestBody(ignoreEmployment = true)

  val request: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId,
    body = ignoreEmploymentRequestBody
  )

  trait Test extends MockIgnoreEmploymentConnector{
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: IgnoreEmploymentService = new IgnoreEmploymentService(
      connector = mockIgnoreEmploymentConnector
    )
  }

  "IgnoreEmploymentService" when {
    "ignoreEmployment" should {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockIgnoreEmploymentConnector.ignoreEmployment(request)
          .returns(Future.successful(outcome))

        await(service.ignoreEmployment(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockIgnoreEmploymentConnector.ignoreEmployment(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.ignoreEmployment(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", NotFoundError),
          ("INVALID_PAYLOAD", DownstreamError),
          ("INVALID_REQUEST_BEFORE_TAX_YEAR_END", RuleTaxYearNotEndedError),
          ("NOT_HMRC_EMPLOYMENT", RuleCustomEmploymentError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}