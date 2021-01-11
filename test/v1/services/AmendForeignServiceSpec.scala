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
import v1.mocks.connectors.MockAmendForeignConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendForeign.{AmendForeignRequest, AmendForeignRequestBody, ForeignEarnings, UnremittableForeignIncomeItem}

import scala.concurrent.Future

class AmendForeignServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019-20"

  private val foreignEarningsModel = ForeignEarnings(
    customerReference = Some("ref"),
    earningsNotTaxableUK = 111.11
  )

  private val unremittableForeignIncomeModel = Seq(
    UnremittableForeignIncomeItem(
      countryCode = "DEU",
      amountInForeignCurrency = 222.22,
      amountTaxPaid = Some(333.33)
    ),
    UnremittableForeignIncomeItem(
      countryCode = "FRA",
      amountInForeignCurrency = 444.44,
      amountTaxPaid = Some(555.55)
    )
  )

  private val amendForeignRequestBody = AmendForeignRequestBody(
    foreignEarnings = Some(foreignEarningsModel),
    unremittableForeignIncome = Some(unremittableForeignIncomeModel)
  )

  val amendForeignRequest: AmendForeignRequest = AmendForeignRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = amendForeignRequestBody
  )

  trait Test extends MockAmendForeignConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AmendForeignService = new AmendForeignService(
      connector = mockAmendForeignConnector
    )
  }

  "AmendForeignService" when {
    "amendForeign" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendForeignConnector.amendForeign(amendForeignRequest)
          .returns(Future.successful(outcome))

        await(service.amendForeign(amendForeignRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAmendForeignConnector.amendForeign(amendForeignRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amendForeign(amendForeignRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
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