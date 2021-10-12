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

import v1r7.controllers.EndpointLogContext
import v1r7.fixtures.overrides.CreateAmendCgtPpdOverridesServiceConnectorFixture.requestBodyModel
import v1r7.mocks.connectors.MockCreateAmendCgtPpdOverridesConnector
import v1r7.models.domain.Nino
import v1r7.models.errors._
import v1r7.models.outcomes.ResponseWrapper
import v1r7.models.request.createAmendCgtPpdOverrides.CreateAmendCgtPpdOverridesRequest

import scala.concurrent.Future

class CreateAmendCgtPpdOverridesServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019-20"

  val createAmendCgtPpdOverridesRequest: CreateAmendCgtPpdOverridesRequest = CreateAmendCgtPpdOverridesRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = requestBodyModel
  )

  trait Test extends MockCreateAmendCgtPpdOverridesConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("Other", "amend")

    val service: CreateAmendCgtPpdOverridesService = new CreateAmendCgtPpdOverridesService(
      connector = mockCreateAmendCgtPpdOverridesConnector
    )
  }

  "CreateAmendCgtPpdOverridesService" when {
    "createAndAmend" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendCgtPpdOverridesConnector.createAmend(createAmendCgtPpdOverridesRequest)
          .returns(Future.successful(outcome))

        await(service.createAmend(createAmendCgtPpdOverridesRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the connector" in new Test {

            MockCreateAmendCgtPpdOverridesConnector.createAmend(createAmendCgtPpdOverridesRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.createAmend(createAmendCgtPpdOverridesRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        def failuresArrayError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the connector in a failures array" in new Test {

            MockCreateAmendCgtPpdOverridesConnector.createAmend(createAmendCgtPpdOverridesRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode(desErrorCode)))))))

            await(service.createAmend(createAmendCgtPpdOverridesRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", DownstreamError),
          ("INVALID_PAYLOAD", DownstreamError),
          ("PPD_SUBMISSIONID_NOT_FOUND", PpdSubmissionIdNotFoundError),
          ("NO_PPD_SUBMISSIONS_FOUND", NotFoundError),
          ("DUPLICATE_SUBMISSION", DownstreamError),
          ("INVALID_REQUEST_BEFORE_TAX_YEAR", RuleTaxYearNotEndedError),
          ("INVALID_DISPOSAL_TYPE", DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
        input.foreach(args => (failuresArrayError _).tupled(args))
      }
    }
  }
}