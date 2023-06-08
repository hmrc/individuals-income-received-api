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
import v2.fixtures.other.CreateAmendOtherFixtures.{requestBodyModel, requestBodyWithPCRJson}
import v2.mocks.validators.MockCreateAmendOtherValidator
import v2.models.request.createAmendOther._

class CreateAmendOtherRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRawRequestBody = AnyContentAsJson(requestBodyWithPCRJson)

  private val createAmendOtherRawData = CreateAmendOtherRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockCreateAmendOtherValidator {

    lazy val parser: CreateAmendOtherRequestParser = new CreateAmendOtherRequestParser(
      validator = mockCreateAmendOtherValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateAmendOtherValidator.validate(createAmendOtherRawData).returns(Nil)

        parser.parseRequest(createAmendOtherRawData) shouldBe
          Right(CreateAmendOtherRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBodyModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockCreateAmendOtherValidator
          .validate(createAmendOtherRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(createAmendOtherRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockCreateAmendOtherValidator
          .validate(createAmendOtherRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(createAmendOtherRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
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
            paths = Some(
              List(
                "/businessReceipts/0/taxYear"
              ))
          ),
          CountryCodeRuleError.copy(
            paths = Some(
              List(
                "/allOtherIncomeReceivedWhilstAbroad/1/countryCode"
              ))
          ),
          RuleTaxYearRangeInvalidError.copy(
            paths = Some(
              List(
                "/businessReceipts/1/taxYear"
              ))
          ),
          CountryCodeFormatError.copy(
            paths = Some(
              List(
                "/allOtherIncomeReceivedWhilstAbroad/0/countryCode"
              ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(
              List(
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

        MockCreateAmendOtherValidator
          .validate(createAmendOtherRawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(createAmendOtherRawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }

}
