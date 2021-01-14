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
import v1.mocks.validators.MockAmendOtherValidator
import v1.models.errors._
import v1.models.request.amendOther._

class AmendOtherRequestParserSpec extends UnitSpec{

  val nino: String = "AA123456B"
  val taxYear: String = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "businessReceipts": [
      |      {
      |         "grossAmount": 5000.99,
      |         "taxYear": "2018-19"
      |      },
      |      {
      |         "grossAmount": 6000.99,
      |         "taxYear": "2019-20"
      |      }
      |   ],
      |   "allOtherIncomeReceivedWhilstAbroad": [
      |      {
      |         "countryCode": "FRA",
      |         "amountBeforeTax": 1999.99,
      |         "taxTakenOff": 2.23,
      |         "specialWithholdingTax": 3.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 4.23,
      |         "residentialFinancialCostAmount": 2999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 1999.99
      |      },
      |      {
      |         "countryCode": "IND",
      |         "amountBeforeTax": 2999.99,
      |         "taxTakenOff": 3.23,
      |         "specialWithholdingTax": 4.23,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 5.23,
      |         "residentialFinancialCostAmount": 3999.99,
      |         "broughtFwdResidentialFinancialCostAmount": 2999.99
      |      }
      |   ],
      |   "overseasIncomeAndGains": {
      |      "gainAmount": 3000.99
      |   },
      |   "chargeableForeignBenefitsAndGifts": {
      |      "transactionBenefit": 1999.99,
      |      "protectedForeignIncomeSourceBenefit": 2999.99,
      |      "protectedForeignIncomeOnwardGift": 3999.99,
      |      "benefitReceivedAsASettler": 4999.99,
      |      "onwardGiftReceivedAsASettler": 5999.99
      |   },
      |   "omittedForeignIncome": {
      |      "amount": 4000.99
      |   }
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)

  private val fullBusinessReceiptsModel = Seq(
    AmendBusinessReceiptsItem(
      grossAmount = 5000.99,
      taxYear = "2018-19"
    ),
    AmendBusinessReceiptsItem(
      grossAmount = 6000.99,
      taxYear = "2019-20"
    )
  )

  private val fullAllOtherIncomeReceivedWhilstAbroadModel = Seq(
    AmendAllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1999.99),
      taxTakenOff = Some(2.23),
      specialWithholdingTax = Some(3.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 4.23,
      residentialFinancialCostAmount = Some(2999.99),
      broughtFwdResidentialFinancialCostAmount = Some(1999.99)
    ),
    AmendAllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "IND",
      amountBeforeTax = Some(2999.99),
      taxTakenOff = Some(3.23),
      specialWithholdingTax = Some(4.23),
      foreignTaxCreditRelief = true,
      taxableAmount = 5.23,
      residentialFinancialCostAmount = Some(3999.99),
      broughtFwdResidentialFinancialCostAmount = Some(2999.99)
    )
  )

  private val fullOverseasIncomeAndGainsModel = AmendOverseasIncomeAndGains(gainAmount = 3000.99)

  private val fullChargeableForeignBenefitsAndGiftsModel = AmendChargeableForeignBenefitsAndGifts(
    transactionBenefit = Some(1999.99),
    protectedForeignIncomeSourceBenefit = Some(2999.99),
    protectedForeignIncomeOnwardGift = Some(3999.99),
    benefitReceivedAsASettler = Some(4999.99),
    onwardGiftReceivedAsASettler = Some(5999.99)
  )

  private val fullOmittedForeignIncomeModel = AmendOmittedForeignIncome(amount = 4000.99)

  private val validRequestBodyModel = AmendOtherRequestBody(
    Some(fullBusinessReceiptsModel),
    Some(fullAllOtherIncomeReceivedWhilstAbroadModel),
    Some(fullOverseasIncomeAndGainsModel),
    Some(fullChargeableForeignBenefitsAndGiftsModel),
    Some(fullOmittedForeignIncomeModel)
  )

  private val amendOtherRawData = AmendOtherRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockAmendOtherValidator {
    lazy val parser: AmendOtherRequestParser = new AmendOtherRequestParser(
      validator = mockAmendOtherValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendOtherValidator.validate(amendOtherRawData).returns(Nil)

        parser.parseRequest(amendOtherRawData) shouldBe
          Right(AmendOtherRequest(Nino(nino), taxYear, validRequestBodyModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendOtherValidator.validate(amendOtherRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(amendOtherRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockAmendOtherValidator.validate(amendOtherRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(amendOtherRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple field value validation errors occur" in new Test {

        private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "businessReceipts": [
            |      {
            |         "grossAmount": 5000.999,
            |         "taxYear": "2019"
            |      },
            |      {
            |         "grossAmount": 6000.999,
            |         "taxYear": "2019-21"
            |      }
            |   ],
            |   "allOtherIncomeReceivedWhilstAbroad": [
            |      {
            |         "countryCode": "FRANCE",
            |         "amountBeforeTax": -1999.99,
            |         "taxTakenOff": -2.23,
            |         "specialWithholdingTax": 3.233,
            |         "foreignTaxCreditRelief": false,
            |         "taxableAmount": 4.233,
            |         "residentialFinancialCostAmount": -2999.99,
            |         "broughtFwdResidentialFinancialCostAmount": 1999.995
            |      },
            |      {
            |         "countryCode": "SBT",
            |         "amountBeforeTax": -2999.99,
            |         "taxTakenOff": -3.23,
            |         "specialWithholdingTax": 4.235,
            |         "foreignTaxCreditRelief": true,
            |         "taxableAmount": 5.253,
            |         "residentialFinancialCostAmount": 3999.959,
            |         "broughtFwdResidentialFinancialCostAmount": -2999.99
            |      }
            |   ],
            |   "overseasIncomeAndGains": {
            |      "gainAmount": 3000.993
            |   },
            |   "chargeableForeignBenefitsAndGifts": {
            |      "transactionBenefit": 1999.992,
            |      "protectedForeignIncomeSourceBenefit": 2999.999,
            |      "protectedForeignIncomeOnwardGift": -3999.99,
            |      "benefitReceivedAsASettler": -4999.99,
            |      "onwardGiftReceivedAsASettler": 5999.996
            |   },
            |   "omittedForeignIncome": {
            |      "amount": -4000.99
            |   }
            |}
          """.stripMargin
        )

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

        private val allInvalidValueErrors = List(
          TaxYearFormatError.copy(
            paths = Some(List(
              "/businessReceipts/0/taxYear"
            ))
          ),
          CountryCodeRuleError.copy(
            paths = Some(List(
              "/allOtherIncomeReceivedWhilstAbroad/1/countryCode"
            ))
          ),
          RuleTaxYearRangeInvalidError.copy(
            paths = Some(List(
              "/businessReceipts/1/taxYear"
            ))
          ),
          CountryCodeFormatError.copy(
            paths = Some(List(
              "/allOtherIncomeReceivedWhilstAbroad/0/countryCode"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(List(
              "/businessReceipts/0/grossAmount",
              "/businessReceipts/1/grossAmount",
              "/allOtherIncomeReceivedWhilstAbroad/0/amountBeforeTax",
              "/allOtherIncomeReceivedWhilstAbroad/0/taxTakenOff",
              "/allOtherIncomeReceivedWhilstAbroad/0/specialWithholdingTax",
              "/allOtherIncomeReceivedWhilstAbroad/0/taxableAmount",
              "/allOtherIncomeReceivedWhilstAbroad/0/residentialFinancialCostAmount",
              "/allOtherIncomeReceivedWhilstAbroad/0/broughtFwdResidentialFinancialCostAmount",
              "/allOtherIncomeReceivedWhilstAbroad/1/amountBeforeTax",
              "/allOtherIncomeReceivedWhilstAbroad/1/taxTakenOff",
              "/allOtherIncomeReceivedWhilstAbroad/1/specialWithholdingTax",
              "/allOtherIncomeReceivedWhilstAbroad/1/taxableAmount",
              "/allOtherIncomeReceivedWhilstAbroad/1/residentialFinancialCostAmount",
              "/allOtherIncomeReceivedWhilstAbroad/1/broughtFwdResidentialFinancialCostAmount",
              "/overseasIncomeAndGains/gainAmount",
              "/chargeableForeignBenefitsAndGifts/transactionBenefit",
              "/chargeableForeignBenefitsAndGifts/protectedForeignIncomeSourceBenefit",
              "/chargeableForeignBenefitsAndGifts/protectedForeignIncomeOnwardGift",
              "/chargeableForeignBenefitsAndGifts/benefitReceivedAsASettler",
              "/chargeableForeignBenefitsAndGifts/onwardGiftReceivedAsASettler",
              "/omittedForeignIncome/amount"
            ))
          )
        )

        MockAmendOtherValidator.validate(amendOtherRawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(amendOtherRawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }
}