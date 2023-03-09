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
import v1.fixtures.residentialPropertyDisposals.CreateAmendCgtResidentialPropertyDisposalsServiceConnectorFixture.requestBody
import v1.mocks.connectors.MockCreateAmendCgtResidentialPropertyDisposalsConnector
import v1.models.request.createAmendCgtResidentialPropertyDisposals.CreateAmendCgtResidentialPropertyDisposalsRequest

import scala.concurrent.Future

class CreateAmendCgtResidentialPropertyDisposalsServiceSpec extends ServiceSpec {

  val createAmendCgtResidentialPropertyDisposalsRequest: CreateAmendCgtResidentialPropertyDisposalsRequest =
    CreateAmendCgtResidentialPropertyDisposalsRequest(
      nino = Nino("AA112233A"),
      taxYear = TaxYear.fromMtd("2019-20"),
      body = requestBody
    )

  trait Test extends MockCreateAmendCgtResidentialPropertyDisposalsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("Other", "amend")

    val service: CreateAmendCgtResidentialPropertyDisposalsService = new CreateAmendCgtResidentialPropertyDisposalsService(
      connector = mockCreateAmendCgtResidentialPropertyDisposalsConnector
    )

  }

  "CreateAmendCgtResidentialPropertyDisposalsService" when {
    "createAndAmend" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendCgtResidentialPropertyDisposalsConnector
          .createAndAmend(createAmendCgtResidentialPropertyDisposalsRequest)
          .returns(Future.successful(outcome))

        await(service.createAndAmend(createAmendCgtResidentialPropertyDisposalsRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the connector" in new Test {

            MockCreateAmendCgtResidentialPropertyDisposalsConnector
              .createAndAmend(createAmendCgtResidentialPropertyDisposalsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.createAndAmend(createAmendCgtResidentialPropertyDisposalsRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("INVALID_PAYLOAD", InternalError),
          ("INVALID_DISPOSAL_DATE", RuleDisposalDateError),
          ("INVALID_COMPLETION_DATE", RuleCompletionDateError),
          ("INVALID_ACQUISITION_DATE", RuleAcquisitionDateAfterDisposalDateError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = Seq(
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
          ("INVALID_CORRELATION_ID", InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
