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
import v1.mocks.connectors.MockAmendSavingsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendSavings.{AmendForeignInterestItem, AmendSavingsRequest, AmendSavingsRequestBody}

import scala.concurrent.Future

class AmendSavingsServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019-20"

  val foreignInterest: AmendForeignInterestItem = AmendForeignInterestItem(
    amountBeforeTax = None,
    countryCode = "FRA",
    taxTakenOff = None,
    specialWithholdingTax = None,
    taxableAmount = 233.11,
    foreignTaxCreditRelief = false
  )

  private val amendSavingsRequest = AmendSavingsRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = AmendSavingsRequestBody(securities = None, foreignInterest = Some(Seq(foreignInterest)))
  )

  trait Test extends MockAmendSavingsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AmendSavingsService = new AmendSavingsService(
      connector = mockAmendSavingsConnector
    )
  }

  "AmendSavingsService" when {
    "amendSaving" must {
      "return correct result for a success" in new Test {
        private val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendSavingsConnector.amendSaving(amendSavingsRequest)
          .returns(Future.successful(outcome))

        await(service.amendSaving(amendSavingsRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAmendSavingsConnector.amendSaving(amendSavingsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amendSaving(amendSavingsRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", DownstreamError),
          ("INVALID_PAYLOAD",  DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}