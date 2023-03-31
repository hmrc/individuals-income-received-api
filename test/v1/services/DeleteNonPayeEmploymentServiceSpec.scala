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
import v1.mocks.connectors.MockDeleteNonPayeEmploymentConnector
import v1.models.request.deleteNonPayeEmployment.DeleteNonPayeEmploymentRequest

import scala.concurrent.Future

class DeleteNonPayeEmploymentServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = "2019-20"

  private val requestData = DeleteNonPayeEmploymentRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  trait Test extends MockDeleteNonPayeEmploymentConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: DeleteNonPayeEmploymentService =
      new DeleteNonPayeEmploymentService(connector = mockDeleteNonPayeEmploymentConnector)

  }

  "DeleteNonPayeEmploymentService" when {
    "deleteNonPayeEmployment" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockDeleteNonPayeEmploymentConnector
          .deleteNonPayeEmployment(requestData)
          .returns(Future.successful(outcome))

        await(service.deleteNonPayeEmployment(requestData)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {
            val outcome = Left(ErrorWrapper(correlationId, error))

            MockDeleteNonPayeEmploymentConnector
              .deleteNonPayeEmployment(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.deleteNonPayeEmployment(requestData)) shouldBe outcome
          }

        val errors = List(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "INVALID_CORRELATIONID"     -> InternalError,
          "NO_DATA_FOUND"             -> NotFoundError,
          "SERVER_ERROR"              -> InternalError,
          "SERVICE_UNAVAILABLE"       -> InternalError
        )

        val extraTysErrors = List(
          "INVALID_CORRELATION_ID" -> InternalError,
          "NOT_FOUND"              -> NotFoundError,
          "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
