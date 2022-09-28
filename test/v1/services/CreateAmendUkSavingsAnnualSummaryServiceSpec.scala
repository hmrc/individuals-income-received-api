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
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.mocks.connectors.MockCreateAmendUkSavingsAnnualSummaryConnector
import v1.models.request.createAmendUkSavingsAnnualSummary.{
  CreateAmendUkSavingsAnnualSummaryBody,
  CreateAmendUkSavingsAnnualSummaryRequest,
  DownstreamCreateAmendUkSavingsAnnualSummaryBody
}

import scala.concurrent.Future

class CreateAmendUkSavingsAnnualSummaryServiceSpec extends ServiceSpec {

  private val nino                     = Nino("AA112233A")
  private val taxYear                  = TaxYear.fromMtd("2019-20")
  private val savingsAccountId: String = "ABC1234567890"

  private val request = CreateAmendUkSavingsAnnualSummaryRequest(
    nino = nino,
    taxYear = taxYear,
    savingsAccountId,
    body = CreateAmendUkSavingsAnnualSummaryBody(None, None)
  )

  private val downstreamBody: DownstreamCreateAmendUkSavingsAnnualSummaryBody = DownstreamCreateAmendUkSavingsAnnualSummaryBody(request)

  trait Test extends MockCreateAmendUkSavingsAnnualSummaryConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("Savings", "amend")

    val service: CreateAmendUkSavingsAnnualSummaryService = new CreateAmendUkSavingsAnnualSummaryService(
      connector = mockAmendUkSavingsConnector
    )

  }

  "CreateAmendUkSavingsAnnualSummaryService" when {
    "the downstream request is successful" must {
      "return a success result" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendUkSavingsAnnualSummaryConnector
          .createOrAmendAnnualSummary(nino, taxYear, downstreamBody)
          .returns(Future.successful(outcome))

        await(service.createAmend(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockCreateAmendUkSavingsAnnualSummaryConnector
              .createOrAmendAnnualSummary(nino, taxYear, downstreamBody)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.createAmend(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TAXYEAR", TaxYearFormatError),
          ("INVALID_TYPE", StandardDownstreamError),
          ("INVALID_PAYLOAD", StandardDownstreamError),
          ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
          ("MISSING_CHARITIES_NAME_GIFT_AID", StandardDownstreamError),
          ("MISSING_GIFT_AID_AMOUNT", StandardDownstreamError),
          ("MISSING_CHARITIES_NAME_INVESTMENT", StandardDownstreamError),
          ("MISSING_INVESTMENT_AMOUNT", StandardDownstreamError),
          ("INVALID_ACCOUNTING_PERIOD", RuleTaxYearNotSupportedError),
          ("GONE", StandardDownstreamError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError)
        )
        val tysErrors = Seq(
          ("INVALID_TAXYEAR"            -> TaxYearFormatError),
          ("INCOME_SOURCE_NOT_FOUND"    -> NotFoundError),
          ("INVALID_INCOMESOURCE_TYPE"  -> StandardDownstreamError),
          ("INVALID_CORRELATIONID"      -> StandardDownstreamError),
          ("INCOMPATIBLE_INCOME_SOURCE" -> StandardDownstreamError),
          ("TAX_YEAR_NOT_SUPPORTED"     -> RuleTaxYearNotSupportedError)
        )

        (errors ++ tysErrors).distinct.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
