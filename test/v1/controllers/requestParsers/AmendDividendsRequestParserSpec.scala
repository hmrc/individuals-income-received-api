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

package v1.controllers.requestParsers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockAmendDividendsValidator
import v1.models.errors._
import v1.models.request.amendDividends._

class AmendDividendsRequestParserSpec extends UnitSpec{

  val nino: String = "AA123456B"
  val taxYear: String = "2019-20"
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

  private val fullDividendIncomeReceivedWhilstAbroadModel = Seq(
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

  private val fullStockDividendModel = AmendCommonDividends(
    customerReference = Some ("my divs"),
    grossAmount = 12321.22
  )

  private val fullRedeemableSharesModel = AmendCommonDividends(
    customerReference = Some ("my shares"),
    grossAmount = 12345.75
  )

  private val fullBonusIssuesOfSecuritiesModel = AmendCommonDividends(
    customerReference = Some ("my secs"),
    grossAmount = 12500.89
  )

  private val fullCloseCompanyLoansWrittenOffModel = AmendCommonDividends(
    customerReference = Some ("write off"),
    grossAmount = 13700.55
  )

  private val validRequestBodyModel = AmendDividendsRequestBody(
    Some(fullForeignDividendModel),
    Some(fullDividendIncomeReceivedWhilstAbroadModel),
    Some(fullStockDividendModel),
    Some(fullRedeemableSharesModel),
    Some(fullBonusIssuesOfSecuritiesModel),
    Some(fullCloseCompanyLoansWrittenOffModel)
  )

  private val amendDividendsRawData = AmendDividendsRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockAmendDividendsValidator {
    lazy val parser: AmendDividendsRequestParser = new AmendDividendsRequestParser(
      validator = mockAmendDividendsValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendDividendsValidator.validate(amendDividendsRawData).returns(Nil)

        parser.parseRequest(amendDividendsRawData) shouldBe
          Right(AmendDividendsRequest(Nino(nino), taxYear, validRequestBodyModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendDividendsValidator.validate(amendDividendsRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(amendDividendsRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockAmendDividendsValidator.validate(amendDividendsRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(amendDividendsRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple field value validation errors occur" in new Test {

        private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "foreignDividend": [
            |      {
            |        "countryCode": "GERMANY",
            |        "amountBeforeTax": -1232.22,
            |        "taxTakenOff": 22.223,
            |        "specialWithholdingTax": 27.354,
            |        "foreignTaxCreditRelief": true,
            |        "taxableAmount": -2321.22
            |      },
            |      {
            |        "countryCode": "PUR",
            |        "amountBeforeTax": 1350.559,
            |        "taxTakenOff": 25.278,
            |        "specialWithholdingTax": -30.59,
            |        "foreignTaxCreditRelief": false,
            |        "taxableAmount": -2500.99
            |      }
            |   ],
            |   "dividendIncomeReceivedWhilstAbroad": [
            |      {
            |        "countryCode": "FRANCE",
            |        "amountBeforeTax": 1232.227,
            |        "taxTakenOff": 22.224,
            |        "specialWithholdingTax": 27.358,
            |        "foreignTaxCreditRelief": true,
            |        "taxableAmount": 2321.229
            |      },
            |      {
            |        "countryCode": "SBT",
            |        "amountBeforeTax": -1350.55,
            |        "taxTakenOff": -25.27,
            |        "specialWithholdingTax": -30.59,
            |        "foreignTaxCreditRelief": false,
            |        "taxableAmount": -2500.99
            |       }
            |   ],
            |   "stockDividend": {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "grossAmount": -12321.22
            |   },
            |   "redeemableShares": {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "grossAmount": 12345.758
            |   },
            |   "bonusIssuesOfSecurities": {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "grossAmount": -12500.89
            |   },
            |   "closeCompanyLoansWrittenOff": {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "grossAmount": 13700.557
            |   }
            |}
          """.stripMargin
        )

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

        private val allInvalidValueErrors = List(
          CountryCodeRuleError.copy(
            paths = Some(List(
              "/foreignDividend/1/countryCode",
              "/dividendIncomeReceivedWhilstAbroad/1/countryCode"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(List(
              "/foreignDividend/0/amountBeforeTax",
              "/foreignDividend/0/taxTakenOff",
              "/foreignDividend/0/specialWithholdingTax",
              "/foreignDividend/0/taxableAmount",
              "/foreignDividend/1/amountBeforeTax",
              "/foreignDividend/1/taxTakenOff",
              "/foreignDividend/1/specialWithholdingTax",
              "/foreignDividend/1/taxableAmount",
              "/dividendIncomeReceivedWhilstAbroad/0/amountBeforeTax",
              "/dividendIncomeReceivedWhilstAbroad/0/taxTakenOff",
              "/dividendIncomeReceivedWhilstAbroad/0/specialWithholdingTax",
              "/dividendIncomeReceivedWhilstAbroad/0/taxableAmount",
              "/dividendIncomeReceivedWhilstAbroad/1/amountBeforeTax",
              "/dividendIncomeReceivedWhilstAbroad/1/taxTakenOff",
              "/dividendIncomeReceivedWhilstAbroad/1/specialWithholdingTax",
              "/dividendIncomeReceivedWhilstAbroad/1/taxableAmount",
              "/stockDividend/grossAmount",
              "/redeemableShares/grossAmount",
              "/bonusIssuesOfSecurities/grossAmount",
              "/closeCompanyLoansWrittenOff/grossAmount"
            ))
          ),
          CustomerRefFormatError.copy(
            paths = Some(List(
              "/stockDividend/customerReference",
              "/redeemableShares/customerReference",
              "/bonusIssuesOfSecurities/customerReference",
              "/closeCompanyLoansWrittenOff/customerReference"
            ))
          ),
          CountryCodeFormatError.copy(
            paths = Some(List(
              "/foreignDividend/0/countryCode",
              "/dividendIncomeReceivedWhilstAbroad/0/countryCode"
            ))
          )
        )

        MockAmendDividendsValidator.validate(amendDividendsRawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(amendDividendsRawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }
}