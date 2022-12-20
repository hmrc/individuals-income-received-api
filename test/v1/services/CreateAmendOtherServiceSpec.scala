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
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, ErrorWrapper, MtdError, NinoFormatError, StandardDownstreamError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.fixtures.other.CreateAmendOtherServiceConnectorFixture.requestBodyModel
import v1.mocks.connectors.MockCreateAmendOtherConnector
import v1.models.request.createAmendOther.CreateAmendOtherRequest

import scala.concurrent.Future

class CreateAmendOtherServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = "2019-20"

  val createAmendOtherRequest: CreateAmendOtherRequest = CreateAmendOtherRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = requestBodyModel
  )

  trait Test extends MockCreateAmendOtherConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("Other", "createAmend")

    val service: CreateAmendOtherService = new CreateAmendOtherService(
      connector = mockCreateAmendOtherConnector
    )

  }

  "CreateAmendOtherService" when {
    "createAmend" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendOtherConnector
          .createAmendOther(createAmendOtherRequest)
          .returns(Future.successful(outcome))

        await(service.createAmend(createAmendOtherRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockCreateAmendOtherConnector
              .createAmendOther(createAmendOtherRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.createAmend(createAmendOtherRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", StandardDownstreamError),
          ("INVALID_PAYLOAD", StandardDownstreamError),
          ("UNPROCESSABLE_ENTITY", StandardDownstreamError),
          ("SERVER_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
