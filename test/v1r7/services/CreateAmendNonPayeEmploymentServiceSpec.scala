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

import v1r7.controllers.EndpointLogContext
import v1r7.fixtures.nonPayeEmployment.CreateAmendNonPayeEmploymentServiceConnectorFixture.requestBodyModel
import v1r7.mocks.connectors.MockCreateAmendNonPayeEmploymentConnector
import v1r7.models.domain.Nino
import v1r7.models.errors._
import v1r7.models.outcomes.ResponseWrapper
import v1r7.models.request.createAmendNonPayeEmployment.CreateAmendNonPayeEmploymentRequest

import scala.concurrent.Future

class CreateAmendNonPayeEmploymentServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = "2019-20"

  val request: CreateAmendNonPayeEmploymentRequest = CreateAmendNonPayeEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = requestBodyModel
  )

  trait Test extends MockCreateAmendNonPayeEmploymentConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("Other", "amend")

    val service: CreateAmendNonPayeEmploymentService = new CreateAmendNonPayeEmploymentService(
      connector = mockConnector
    )
  }

  "createAndAmend" must {
    "return correct result for a success" in new Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      MockCreateAmendNonPayeEmploymentConnector
        .createAndAmend(request)
        .returns(Future.successful(outcome))

      await(service.createAndAmend(request)) shouldBe outcome
    }

    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the connector" in new Test {

          MockCreateAmendNonPayeEmploymentConnector
            .createAndAmend(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.createAndAmend(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      def failuresArrayError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the connector in a failures array" in new Test {

          MockCreateAmendNonPayeEmploymentConnector
            .createAndAmend(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors(List(DesErrorCode(desErrorCode)))))))

          await(service.createAndAmend(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INVALID_CORRELATIONID", DownstreamError),
        ("INVALID_PAYLOAD", DownstreamError),
        ("NO_DATA_FOUND", NotFoundError),
        ("INVALID_REQUEST_BEFORE_TAX_YEAR", RuleTaxYearNotEndedError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
      input.foreach(args => (failuresArrayError _).tupled(args))
    }
  }
}
