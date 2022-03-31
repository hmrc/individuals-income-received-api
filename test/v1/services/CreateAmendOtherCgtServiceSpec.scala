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
import v1.fixtures.other.CreateAmendOtherCgtConnectorServiceFixture.mtdRequestBody
import v1.mocks.connectors.MockCreateAmendOtherCgtConnector
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import v1.models.request.createAmendOtherCgt.CreateAmendOtherCgtRequest

import scala.concurrent.Future
import api.services.ServiceSpec

class CreateAmendOtherCgtServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = "2019-20"

  val createAmendOtherCgtRequest: CreateAmendOtherCgtRequest = CreateAmendOtherCgtRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = mtdRequestBody
  )

  trait Test extends MockCreateAmendOtherCgtConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("Other", "amend")

    val service: CreateAmendOtherCgtService = new CreateAmendOtherCgtService(
      connector = mockCreateAmendOtherCgtDisposalsAndGainsConnector
    )

  }

  "createAndAmend" should {
    "return a success response" when {
      "a valid request is made" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))
        MockCreateAmendOtherCgtConnector
          .createAndAmend(createAmendOtherCgtRequest)
          .returns(Future.successful(outcome))

        await(service.createAmend(createAmendOtherCgtRequest)) shouldBe outcome
      }
    }

    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the connector" in new Test {

          MockCreateAmendOtherCgtConnector
            .createAndAmend(createAmendOtherCgtRequest)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

          await(service.createAmend(createAmendOtherCgtRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      def failuresArrayError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error inside 'failures' array element is returned from the connector " in new Test {

          MockCreateAmendOtherCgtConnector
            .createAndAmend(createAmendOtherCgtRequest)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode(desErrorCode)))))))

          await(service.createAmend(createAmendOtherCgtRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INVALID_CORRELATIONID", StandardDownstreamError),
        ("INVALID_PAYLOAD", StandardDownstreamError),
        ("INVALID_DISPOSAL_DATE", StandardDownstreamError),
        ("INVALID_ACQUISITION_DATE", StandardDownstreamError),
        ("SERVER_ERROR", StandardDownstreamError),
        ("SERVICE_UNAVAILABLE", StandardDownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
      input.foreach(args => (failuresArrayError _).tupled(args))
    }
  }

}
