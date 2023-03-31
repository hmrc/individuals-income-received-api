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
import v1.fixtures.nonPayeEmployment.CreateAmendNonPayeEmploymentServiceConnectorFixture.requestBodyModel
import v1.mocks.connectors.MockCreateAmendNonPayeEmploymentConnector
import v1.models.request.createAmendNonPayeEmployment.CreateAmendNonPayeEmploymentRequest

import scala.concurrent.Future

class CreateAmendNonPayeEmploymentServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = TaxYear.fromMtd("2019-20")

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

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the connector" in new Test {

          MockCreateAmendNonPayeEmploymentConnector
            .createAndAmend(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.createAndAmend(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INVALID_CORRELATIONID", InternalError),
        ("INVALID_PAYLOAD", InternalError),
        ("NO_DATA_FOUND", NotFoundError),
        ("INVALID_REQUEST_BEFORE_TAX_YEAR", RuleTaxYearNotEndedError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      val extraTysErrors = List(
        "INVALID_CORRELATION_ID" -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
