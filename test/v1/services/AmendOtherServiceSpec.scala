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
import v1.fixtures.other.AmendOtherServiceConnectorFixture.requestBodyModel
import v1.mocks.connectors.MockAmendOtherConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendOther.AmendOtherRequest

import scala.concurrent.Future

class AmendOtherServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019-20"

  val amendOtherRequest: AmendOtherRequest = AmendOtherRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = requestBodyModel
  )

  trait Test extends MockAmendOtherConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("Other", "amend")

    val service: AmendOtherService = new AmendOtherService(
      connector = mockAmendOtherConnector
    )
  }

  "AmendOtherService" when {
    "amend" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendOtherConnector.amendOther(amendOtherRequest)
          .returns(Future.successful(outcome))

        await(service.amend(amendOtherRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAmendOtherConnector.amendOther(amendOtherRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amend(amendOtherRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", DownstreamError),
          ("INVALID_PAYLOAD", DownstreamError),
          ("UNPROCESSABLE_ENTITY", DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}