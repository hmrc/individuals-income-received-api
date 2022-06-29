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
import api.models.outcomes.{DesResponse, ResponseWrapper, RetrieveSavingsAccountAnnualSummaryOutcome}
import api.services.ServiceSpec
import v1.mocks.connectors.MockRetrieveUkSavingsAccountAnnualSummaryConnector
import v1.models.request.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryRequest
import v1.models.response.retrieveUkSavingsAnnualSummary.{DownstreamUkSavingsAnnualIncomeItem, DownstreamUkSavingsAnnualIncomeResponse, RetrieveUkSavingsAnnualSummaryResponse}

import scala.concurrent.Future

class RetrieveUkSavingsAccountAnnualSummaryServiceSpec extends ServiceSpec {

  private val nino             = "AA112233A"
  private val taxYear          = "2019-20"
  private val incomeSourceId   = "SAVKB2UVwUTBQGJ"

  private val requestData = RetrieveUkSavingsAnnualSummaryRequest(Nino(nino), TaxYear.fromMtd(taxYear), incomeSourceId)

  private val validResponse = RetrieveUkSavingsAnnualSummaryResponse(
    taxedUkInterest = Some(1230.55),
    untaxedUkInterest = Some(1230.55)
  )

  trait Test extends MockRetrieveUkSavingsAccountAnnualSummaryConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: RetrieveUkSavingsAccountAnnualSummaryService =
      new RetrieveUkSavingsAccountAnnualSummaryService(connector = mockRetrieveUkSavingsAccountAnnualSummaryConnector)
  }

  "RetrieveUkSavingsAccountAnnualSummaryService" when {
    "retrieveUkSavingsAccountAnnualSummary" must {
      "return correct result for a success" in new Test {
        val request: RetrieveUkSavingsAnnualSummaryRequest = RetrieveUkSavingsAnnualSummaryRequest(Nino(nino), TaxYear.fromMtd(taxYear), incomeSourceId)
        "valid data is passed" should {
          "return a valid response" in new Test {
            val desResponse: DesResponse[DownstreamUkSavingsAnnualIncomeResponse] =
              DesResponse(
                correlationId,
                DownstreamUkSavingsAnnualIncomeResponse(
                  Seq(
                    DownstreamUkSavingsAnnualIncomeResponse(Seq[DownstreamUkSavingsAnnualIncomeItem])
                  )))

            MockRetrieveUkSavingsAccountAnnualSummaryConnector.retrieveUkSavingsAccountAnnualSummary(request).returns(Future.successful(Right(desResponse)))

            val result: RetrieveSavingsAccountAnnualSummaryOutcome = await(service.retrieveUkSavingsAccountAnnualSummary(request))
            result shouldBe Right(DesResponse(correlationId, RetrieveUkSavingsAnnualSummaryResponse(Some(2000.99), Some(5000.50))))
          }
        }
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrieveUkSavingsAccountAnnualSummaryConnector
              .retrieveUkSavingsAccountAnnualSummary(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.retrieveUkSavingsAccountAnnualSummary(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TYPE", StandardDownstreamError),
          ("INVALID_TAXYEAR", TaxYearFormatError),
          ("INVALID_INCOME_SOURCE", SavingsAccountIdFormatError),
          ("NOT_FOUND_PERIOD", NotFoundError),
          ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
          ("SERVER_ERROR", StandardDownstreamError),
          ("SERVICE_UNAVAILABLE", StandardDownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
