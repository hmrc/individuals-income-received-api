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
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.mocks.connectors.MockUnignoreEmploymentConnector
import v1.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.Future

class UnignoreEmploymentServiceSpec extends ServiceSpec {

  private val nino         = "AA112233A"
  private val taxYear      = "2021-22"
  private val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val request: IgnoreEmploymentRequest = IgnoreEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId
  )

  trait Test extends MockUnignoreEmploymentConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: UnignoreEmploymentService = new UnignoreEmploymentService(
      connector = mockUnignoreEmploymentConnector
    )

  }

  "UnignoreEmploymentService" when {
    "unignoreEmployment" should {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockUnignoreEmploymentConnector
          .unignoreEmployment(request)
          .returns(Future.successful(outcome))

        await(service.unignoreEmployment(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockUnignoreEmploymentConnector
              .unignoreEmployment(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.unignoreEmployment(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", EmploymentIdFormatError),
          ("INVALID_CORRELATIONID", StandardDownstreamError),
          ("CUSTOMER_ADDED", RuleCustomEmploymentUnignoreError),
          ("NO_DATA_FOUND", NotFoundError),
          ("BEFORE_TAX_YEAR_ENDED", RuleTaxYearNotEndedError),
          ("SERVER_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
