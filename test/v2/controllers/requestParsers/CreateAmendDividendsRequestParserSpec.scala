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

package v2.controllers.requestParsers

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v2.mocks.validators.MockCreateAmendDividendsValidator
import v2.models.request.createAmendDividends._

class CreateAmendDividendsRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |      }
      |   ],
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |       }
      |   ],
      |   "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": 12321.22
      |   },
      |   "redeemableShares": {
      |      "customerReference": "my shares",
      |      "grossAmount": 12345.75
      |   },
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "my secs",
      |      "grossAmount": 12500.89
      |   },
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off",
      |      "grossAmount": 13700.55
      |   }
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)

  private val fullForeignDividendModel = Seq(
    CreateAmendForeignDividendItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = Some(true),
      taxableAmount = 2321.22
    ),
    CreateAmendForeignDividendItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = Some(false),
      taxableAmount = 2500.99
    )
  )

  private val fullDividendIncomeReceivedWhilstAbroadModel = Seq(
    CreateAmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = Some(true),
      taxableAmount = 2321.22
    ),
    CreateAmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = Some(false),
      taxableAmount = 2500.99
    )
  )

  private val fullStockDividendModel = CreateAmendCommonDividends(
    customerReference = Some("my divs"),
    grossAmount = 12321.22
  )

  private val fullRedeemableSharesModel = CreateAmendCommonDividends(
    customerReference = Some("my shares"),
    grossAmount = 12345.75
  )

  private val fullBonusIssuesOfSecuritiesModel = CreateAmendCommonDividends(
    customerReference = Some("my secs"),
    grossAmount = 12500.89
  )

  private val fullCloseCompanyLoansWrittenOffModel = CreateAmendCommonDividends(
    customerReference = Some("write off"),
    grossAmount = 13700.55
  )

  private val validRequestBodyModel = CreateAmendDividendsRequestBody(
    Some(fullForeignDividendModel),
    Some(fullDividendIncomeReceivedWhilstAbroadModel),
    Some(fullStockDividendModel),
    Some(fullRedeemableSharesModel),
    Some(fullBonusIssuesOfSecuritiesModel),
    Some(fullCloseCompanyLoansWrittenOffModel)
  )

  private val createAmendDividendsRawData = CreateAmendDividendsRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockCreateAmendDividendsValidator {

    lazy val parser: CreateAmendDividendsRequestParser = new CreateAmendDividendsRequestParser(
      validator = mockCreateAmendDividendsValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateAmendDividendsValidator.validate(createAmendDividendsRawData).returns(Nil)

        parser.parseRequest(createAmendDividendsRawData) shouldBe
          Right(CreateAmendDividendsRequest(Nino(nino), TaxYear.fromMtd(taxYear), validRequestBodyModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockCreateAmendDividendsValidator
          .validate(createAmendDividendsRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(createAmendDividendsRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockCreateAmendDividendsValidator
          .validate(createAmendDividendsRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(createAmendDividendsRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
