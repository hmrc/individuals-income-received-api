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
import v1.mocks.connectors.MockRetrieveUkSavingsAccountAnnualSummaryConnector
import v1.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRequest
import v1.models.response.retrieveUkSavingsAnnualSummary.{DownstreamUkSavingsAnnualIncomeItem, DownstreamUkSavingsAnnualIncomeResponse, RetrieveUkSavingsAnnualSummaryResponse}

import scala.concurrent.Future

class RetrieveUkSavingsAccountAnnualSummaryServiceSpec extends ServiceSpec {

  private val nino           = "AA112233A"
  private val taxYear        = "2019-20"
  private val incomeSourceId = "SAVKB2UVwUTBQGJ"

  private val request = RetrieveUkSavingsAnnualSummaryRequest(Nino(nino), TaxYear.fromMtd(taxYear), incomeSourceId)

  trait Test extends MockRetrieveUkSavingsAccountAnnualSummaryConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: RetrieveUkSavingsAccountAnnualSummaryService =
      new RetrieveUkSavingsAccountAnnualSummaryService(connector = mockRetrieveUkSavingsAccountAnnualSummaryConnector)

  }

  "RetrieveUkSavingsAccountAnnualSummaryService" when {
    "the downstream returns a single account" must {
      "return a successful result" in new Test {
        val downstreamResponse: ResponseWrapper[DownstreamUkSavingsAnnualIncomeResponse] =
          ResponseWrapper(
            correlationId,
            DownstreamUkSavingsAnnualIncomeResponse(
              Seq(
                DownstreamUkSavingsAnnualIncomeItem(incomeSourceId = "ignored", taxedUkInterest = Some(2000.99), untaxedUkInterest = Some(5000.50))))
          )

        MockRetrieveUkSavingsAccountAnnualSummaryConnector
          .retrieveUkSavingsAccountAnnualSummary(request) returns Future.successful(Right(downstreamResponse))

        await(service.retrieveUkSavingsAccountAnnualSummary(request)) shouldBe
          Right(ResponseWrapper(correlationId, RetrieveUkSavingsAnnualSummaryResponse(Some(2000.99), Some(5000.50))))
      }
    }

    "the downstream returns multiple accounts" must {
      "return an internal server error" in new Test {
        val downstreamResponse: ResponseWrapper[DownstreamUkSavingsAnnualIncomeResponse] =
          ResponseWrapper(
            correlationId,
            DownstreamUkSavingsAnnualIncomeResponse(
              Seq(
                DownstreamUkSavingsAnnualIncomeItem(incomeSourceId = "ignored1", taxedUkInterest = Some(1), untaxedUkInterest = Some(1)),
                DownstreamUkSavingsAnnualIncomeItem(incomeSourceId = "ignored1", taxedUkInterest = Some(2), untaxedUkInterest = Some(3))
              )
            )
          )

        MockRetrieveUkSavingsAccountAnnualSummaryConnector
          .retrieveUkSavingsAccountAnnualSummary(request) returns Future.successful(Right(downstreamResponse))

        await(service.retrieveUkSavingsAccountAnnualSummary(request)) shouldBe
          Left(ErrorWrapper(correlationId, InternalError))
      }
    }

    "the downstream returns no accounts" must {
      "return a NotFoundError" in new Test {
        val downstreamResponse: ResponseWrapper[DownstreamUkSavingsAnnualIncomeResponse] =
          ResponseWrapper(correlationId, DownstreamUkSavingsAnnualIncomeResponse(Nil))

        MockRetrieveUkSavingsAccountAnnualSummaryConnector
          .retrieveUkSavingsAccountAnnualSummary(request) returns Future.successful(Right(downstreamResponse))

        await(service.retrieveUkSavingsAccountAnnualSummary(request)) shouldBe
          Left(ErrorWrapper(correlationId, NotFoundError))
      }
    }

    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveUkSavingsAccountAnnualSummaryConnector
            .retrieveUkSavingsAccountAnnualSummary(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieveUkSavingsAccountAnnualSummary(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = List(
        ("INVALID_NINO", NinoFormatError),
        ("INVALID_TYPE", InternalError),
        ("INVALID_TAXYEAR", TaxYearFormatError),
        ("INVALID_INCOME_SOURCE", SavingsAccountIdFormatError),
        ("NOT_FOUND_PERIOD", NotFoundError),
        ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      val tysErrors = List(
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INVALID_CORRELATION_ID", InternalError),
        ("INVALID_INCOMESOURCE_ID", SavingsAccountIdFormatError),
        ("INVALID_INCOMESOURCE_TYPE", InternalError),
        ("SUBMISSION_PERIOD_NOT_FOUND", NotFoundError),
        ("INCOME_DATA_SOURCE_NOT_FOUND", NotFoundError),
        ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
      )

      (errors ++ tysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
