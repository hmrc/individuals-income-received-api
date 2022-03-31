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

package v1.services

import api.controllers.EndpointLogContext
import v1.fixtures.overrides.CreateAmendCgtPpdOverridesServiceConnectorFixture.requestBodyModel
import v1.mocks.connectors.MockCreateAmendCgtPpdOverridesConnector
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import v1.models.request.createAmendCgtPpdOverrides.CreateAmendCgtPpdOverridesRequest

import scala.concurrent.Future
import api.services.ServiceSpec

class CreateAmendCgtPpdOverridesServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
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

        MockCreateAmendCgtPpdOverridesConnector
          .createAmend(createAmendCgtPpdOverridesRequest)
          .returns(Future.successful(outcome))

        await(service.createAmend(createAmendCgtPpdOverridesRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the connector" in new Test {

            MockCreateAmendCgtPpdOverridesConnector
              .createAmend(createAmendCgtPpdOverridesRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.createAmend(createAmendCgtPpdOverridesRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        def failuresArrayError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the connector in a failures array" in new Test {

            MockCreateAmendCgtPpdOverridesConnector
              .createAmend(createAmendCgtPpdOverridesRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode(desErrorCode)))))))

            await(service.createAmend(createAmendCgtPpdOverridesRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", StandardDownstreamError),
          ("INVALID_PAYLOAD", StandardDownstreamError),
          ("PPD_SUBMISSIONID_NOT_FOUND", PpdSubmissionIdNotFoundError),
          ("NO_PPD_SUBMISSIONS_FOUND", NotFoundError),
          ("DUPLICATE_SUBMISSION", RuleDuplicatedPpdSubmissionIdError),
          ("INVALID_REQUEST_BEFORE_TAX_YEAR", RuleTaxYearNotEndedError),
          ("INVALID_DISPOSAL_TYPE", RuleIncorrectDisposalTypeError),
          ("SERVER_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
        input.foreach(args => (failuresArrayError _).tupled(args))
      }
    }
  }

}
