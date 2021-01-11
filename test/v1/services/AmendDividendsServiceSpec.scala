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
import v1.mocks.connectors.MockAmendDividendsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendDividends._

import scala.concurrent.Future

class AmendDividendsServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019-20"

  private val foreignDividendModel = Seq(
    AmendForeignDividendItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    ),
    AmendForeignDividendItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = false,
      taxableAmount = 2500.99
    )
  )

  private val dividendIncomeReceivedWhilstAbroadModel = Seq(
    AmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = true,
      taxableAmount = 2321.22
    ),
    AmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = false,
      taxableAmount = 2500.99
    )
  )

  private val stockDividendModel = AmendCommonDividends(
    customerReference = Some("my divs"),
    grossAmount = 12321.22
  )

  private val redeemableSharesModel = AmendCommonDividends(
    customerReference = Some("my shares"),
    grossAmount = 12345.75
  )

  private val bonusIssuesOfSecuritiesModel = AmendCommonDividends(
    customerReference = Some("my secs"),
    grossAmount = 12500.89
  )

  private val closeCompanyLoansWrittenOffModel = AmendCommonDividends(
    customerReference = Some("write off"),
    grossAmount = 13700.55
  )

  val amendDividendsRequest: AmendDividendsRequest = AmendDividendsRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = AmendDividendsRequestBody(
        Some(foreignDividendModel),
        Some(dividendIncomeReceivedWhilstAbroadModel),
        Some(stockDividendModel),
        Some(redeemableSharesModel),
        Some(bonusIssuesOfSecuritiesModel),
        Some(closeCompanyLoansWrittenOffModel)
      )
  )

  trait Test extends MockAmendDividendsConnector{
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AmendDividendsService = new AmendDividendsService(
      connector = mockAmendDividendsConnector
    )
  }

  "AmendDividendsService" when {
    "amendDividends" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendDividendsConnector.amendDividends(amendDividendsRequest)
          .returns(Future.successful(outcome))

        await(service.amendDividends(amendDividendsRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAmendDividendsConnector.amendDividends(amendDividendsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amendDividends(amendDividendsRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", DownstreamError),
          ("INVALID_PAYLOAD", DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}