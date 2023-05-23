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

package v1andv2.services

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1andv2.mocks.connectors.MockAmendFinancialDetailsConnector
import v1andv2.models.request.amendFinancialDetails.emploment.{AmendEmployment, AmendPay}
import v1andv2.models.request.amendFinancialDetails.{AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}

import scala.concurrent.Future

class AmendFinancialDetailsServiceSpec extends ServiceSpec {

  private val nino         = "AA112233A"
  private val taxYear      = "2019-20"
  private val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val payModel = AmendPay(
    taxablePayToDate = 3500.75,
    totalTaxToDate = 6782.92
  )

  private val employmentModel = AmendEmployment(
    pay = payModel,
    deductions = None,
    benefitsInKind = None,
    offPayrollWorker = Some(true)
  )

  private val requestBody = AmendFinancialDetailsRequestBody(
    employment = employmentModel
  )

  val request: AmendFinancialDetailsRequest = AmendFinancialDetailsRequest(Nino(nino), TaxYear.fromMtd(taxYear), employmentId, requestBody)

  "AmendFinancialDetailsService" when {
    "amendFinancialDetails" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendFinancialDetailsConnector
          .amendFinancialDetails(request)
          .returns(Future.successful(outcome))

        await(service.amendFinancialDetails(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockAmendFinancialDetailsConnector
              .amendFinancialDetails(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.amendFinancialDetails(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", NotFoundError),
          ("INVALID_PAYLOAD", InternalError),
          ("BEFORE_TAX_YEAR_END", RuleTaxYearNotEndedError),
          ("INVALID_CORRELATIONID", InternalError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = List(
          ("INCOME_SOURCE_NOT_FOUND", NotFoundError),
          ("INVALID_CORRELATION_ID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockAmendFinancialDetailsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AmendFinancialDetailsService = new AmendFinancialDetailsService(
      connector = mockAmendFinancialDetailsConnector
    )

  }

}
