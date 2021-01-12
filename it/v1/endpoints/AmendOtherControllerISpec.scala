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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendOtherControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
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

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/other/$nino/$taxYear",
         |         "rel":"create-and-amend-other-income",
         |         "method":"PUT"
         |      },
         |      {
         |         "href":"/individuals/income-received/other/$nino/$taxYear",
         |         "rel":"self",
         |         "method":"GET"
         |      },
         |      {
         |         "href":"/individuals/income-received/other/$nino/$taxYear",
         |         "rel":"delete-other-income",
         |         "method":"DELETE"
         |      }
         |   ]
         |}
    """.stripMargin
    )

    def uri: String = s"/other/$nino/$taxYear"

    def desUri: String = s"/income-tax/income/other/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'amend other income' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a TaxYearFormatError" when {
      "a request body having invalid tax year format is supplied" in new Test {

        val invalidRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "businessReceipts": [
            |      {
            |         "grossAmount": 5000.99,
            |         "taxYear": "2018-193"
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

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(invalidRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(ErrorWrapper(
          correlationId = correlationId,
          error = TaxYearFormatError.copy(
            paths = Some(List("/businessReceipts/0/taxYear"))
          ),
          errors = None
        ))
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a RuleTaxYearRangeInvalidError" when {
      "a request body having invalid tax year range is supplied" in new Test {

        val invalidRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "businessReceipts": [
            |      {
            |         "grossAmount": 5000.99,
            |         "taxYear": "2018-23"
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

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(invalidRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(ErrorWrapper(
          correlationId = correlationId,
          error = RuleTaxYearRangeInvalidError.copy(
            paths = Some(List("/businessReceipts/0/taxYear"))
          ),
          errors = None
        ))

        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new Test {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "businessReceipts":[
            |      {
            |         "grossAmount":5000.999,
            |         "taxYear":"2019"
            |      },
            |      {
            |         "grossAmount":6000.999,
            |         "taxYear":"2019-21"
            |      }
            |   ],
            |   "allOtherIncomeReceivedWhilstAbroad":[
            |      {
            |         "countryCode":"FRANCE",
            |         "amountBeforeTax":-1999.99,
            |         "taxTakenOff":-2.23,
            |         "specialWithholdingTax":3.233,
            |         "foreignTaxCreditRelief":false,
            |         "taxableAmount":4.233,
            |         "residentialFinancialCostAmount":-2999.99,
            |         "broughtFwdResidentialFinancialCostAmount":1999.995
            |      },
            |      {
            |         "countryCode":"SBT",
            |         "amountBeforeTax":-2999.99,
            |         "taxTakenOff":-3.23,
            |         "specialWithholdingTax":4.235,
            |         "foreignTaxCreditRelief":true,
            |         "taxableAmount":5.253,
            |         "residentialFinancialCostAmount":3999.959,
            |         "broughtFwdResidentialFinancialCostAmount":-2999.99
            |      }
            |   ],
            |   "overseasIncomeAndGains":{
            |      "gainAmount":3000.993
            |   },
            |   "chargeableForeignBenefitsAndGifts":{
            |      "transactionBenefit":1999.992,
            |      "protectedForeignIncomeSourceBenefit":2999.999,
            |      "protectedForeignIncomeOnwardGift":-3999.99,
            |      "benefitReceivedAsASettler":-4999.99,
            |      "onwardGiftReceivedAsASettler":5999.996
            |   },
            |   "omittedForeignIncome":{
            |      "amount":-4000.99
            |   }
            |}
            |""".stripMargin
        )

        val allInvalidValueRequestError: List[MtdError] = List(
          TaxYearFormatError.copy(
            paths = Some(List("/businessReceipts/0/taxYear"))
          ),
          CountryCodeRuleError.copy(
            paths = Some(List("/allOtherIncomeReceivedWhilstAbroad/1/countryCode"))
          ),
          RuleTaxYearRangeInvalidError.copy(
            paths = Some(List("/businessReceipts/1/taxYear"))
          ),
          CountryCodeFormatError.copy(
            paths = Some(List("/allOtherIncomeReceivedWhilstAbroad/0/countryCode"))
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

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidValueRequestError)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(allInvalidValueRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }

      "complex error scenario" in new Test {

        val iirOtherIncomeAmendErrorsRequest: JsValue = Json.parse(
          """
            |{
            |   "businessReceipts":[
            |      {
            |         "grossAmount":5000.999,
            |         "taxYear":"2019"
            |      },
            |      {
            |         "grossAmount":6000.999,
            |         "taxYear":"2019-21"
            |      }
            |   ],
            |   "allOtherIncomeReceivedWhilstAbroad":[
            |      {
            |         "countryCode":"FRANCE",
            |         "amountBeforeTax":-1999.99,
            |         "taxTakenOff":-2.23,
            |         "specialWithholdingTax":3.233,
            |         "foreignTaxCreditRelief":false,
            |         "taxableAmount":4.233,
            |         "residentialFinancialCostAmount":-2999.99,
            |         "broughtFwdResidentialFinancialCostAmount":1999.995
            |      },
            |      {
            |         "countryCode":"SBT",
            |         "amountBeforeTax":-2999.99,
            |         "taxTakenOff":-3.23,
            |         "specialWithholdingTax":4.235,
            |         "foreignTaxCreditRelief":true,
            |         "taxableAmount":5.253,
            |         "residentialFinancialCostAmount":3999.959,
            |         "broughtFwdResidentialFinancialCostAmount":-2999.99
            |      }
            |   ],
            |   "overseasIncomeAndGains":{
            |      "gainAmount":3000.993
            |   },
            |   "chargeableForeignBenefitsAndGifts":{
            |      "transactionBenefit":1999.992,
            |      "protectedForeignIncomeSourceBenefit":2999.999,
            |      "protectedForeignIncomeOnwardGift":-3999.99,
            |      "benefitReceivedAsASettler":-4999.99,
            |      "onwardGiftReceivedAsASettler":5999.996
            |   },
            |   "omittedForeignIncome":{
            |      "amount":-4000.99
            |   }
            |}
            |""".stripMargin
        )

        val iirOtherIncomeAmendErrorsResponse: JsValue = Json.parse(
          """
            {
            |    "code": "INVALID_REQUEST",
            |    "errors": [
            |        {
            |            "code": "FORMAT_TAX_YEAR",
            |            "message": "The provided tax year is invalid",
            |            "paths": [
            |                "/businessReceipts/0/taxYear"
            |            ]
            |        },
            |        {
            |            "code": "RULE_COUNTRY_CODE",
            |            "message": "The country code is not a valid ISO 3166-1 alpha-3 country code",
            |            "paths": [
            |                "/allOtherIncomeReceivedWhilstAbroad/1/countryCode"
            |            ]
            |        },
            |        {
            |            "code": "RULE_TAX_YEAR_RANGE_INVALID",
            |            "message": "Tax year range invalid. A tax year range of one year is required",
            |            "paths": [
            |                "/businessReceipts/1/taxYear"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_COUNTRY_CODE",
            |            "message": "The format of the country code is invalid",
            |            "paths": [
            |                "/allOtherIncomeReceivedWhilstAbroad/0/countryCode"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_VALUE",
            |            "message": "The field should be between 0 and 99999999999.99",
            |            "paths": [
            |                "/businessReceipts/0/grossAmount",
            |                "/businessReceipts/1/grossAmount",
            |                "/allOtherIncomeReceivedWhilstAbroad/0/amountBeforeTax",
            |                "/allOtherIncomeReceivedWhilstAbroad/0/taxTakenOff",
            |                "/allOtherIncomeReceivedWhilstAbroad/0/specialWithholdingTax",
            |                "/allOtherIncomeReceivedWhilstAbroad/0/taxableAmount",
            |                "/allOtherIncomeReceivedWhilstAbroad/0/residentialFinancialCostAmount",
            |                "/allOtherIncomeReceivedWhilstAbroad/0/broughtFwdResidentialFinancialCostAmount",
            |                "/allOtherIncomeReceivedWhilstAbroad/1/amountBeforeTax",
            |                "/allOtherIncomeReceivedWhilstAbroad/1/taxTakenOff",
            |                "/allOtherIncomeReceivedWhilstAbroad/1/specialWithholdingTax",
            |                "/allOtherIncomeReceivedWhilstAbroad/1/taxableAmount",
            |                "/allOtherIncomeReceivedWhilstAbroad/1/residentialFinancialCostAmount",
            |                "/allOtherIncomeReceivedWhilstAbroad/1/broughtFwdResidentialFinancialCostAmount",
            |                "/overseasIncomeAndGains/gainAmount",
            |                "/chargeableForeignBenefitsAndGifts/transactionBenefit",
            |                "/chargeableForeignBenefitsAndGifts/protectedForeignIncomeSourceBenefit",
            |                "/chargeableForeignBenefitsAndGifts/protectedForeignIncomeOnwardGift",
            |                "/chargeableForeignBenefitsAndGifts/benefitReceivedAsASettler",
            |                "/chargeableForeignBenefitsAndGifts/onwardGiftReceivedAsASettler",
            |                "/omittedForeignIncome/amount"
            |            ]
            |        }
            |    ],
            |    "message": "Invalid request"
            |}
            |""".stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(iirOtherIncomeAmendErrorsRequest))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe iirOtherIncomeAmendErrorsResponse
      }
    }

    "return error according to spec" when {

      val validRequestBodyJson: JsValue = Json.parse(
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

      val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "allOtherIncomeReceivedWhilstAbroad": [
          |      {
          |         "countryCode": "FRANCE",
          |         "amountBeforeTax": 1999.99,
          |         "taxTakenOff": 2.23,
          |         "specialWithholdingTax": 3.23,
          |         "foreignTaxCreditRelief": false,
          |         "taxableAmount": 4.23,
          |         "residentialFinancialCostAmount": 2999.99,
          |         "broughtFwdResidentialFinancialCostAmount": 1999.99
          |      },
          |      {
          |         "countryCode": "INDIA",
          |         "amountBeforeTax": 2999.99,
          |         "taxTakenOff": 3.23,
          |         "specialWithholdingTax": 4.23,
          |         "foreignTaxCreditRelief": true,
          |         "taxableAmount": 5.23,
          |         "residentialFinancialCostAmount": 3999.99,
          |         "broughtFwdResidentialFinancialCostAmount": 2999.99
          |      }
          |   ]
          |}""".stripMargin
      )

      val ruleCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "allOtherIncomeReceivedWhilstAbroad": [
          |      {
          |         "countryCode": "SBT",
          |         "amountBeforeTax": 1999.99,
          |         "taxTakenOff": 2.23,
          |         "specialWithholdingTax": 3.23,
          |         "foreignTaxCreditRelief": false,
          |         "taxableAmount": 4.23,
          |         "residentialFinancialCostAmount": 2999.99,
          |         "broughtFwdResidentialFinancialCostAmount": 1999.99
          |      },
          |      {
          |         "countryCode": "ORK",
          |         "amountBeforeTax": 2999.99,
          |         "taxTakenOff": 3.23,
          |         "specialWithholdingTax": 4.23,
          |         "foreignTaxCreditRelief": true,
          |         "taxableAmount": 5.23,
          |         "residentialFinancialCostAmount": 3999.99,
          |         "broughtFwdResidentialFinancialCostAmount": 2999.99
          |      }
          |   ]
          |}""".stripMargin
      )

      val nonsenseRequestBody: JsValue = Json.parse(
        """
          |{
          |  "field": "value"
          |}
        """.stripMargin
      )

      val allInvalidValueRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "businessReceipts": [
          |      {
          |         "grossAmount": 5000.999,
          |         "taxYear": "2018-19"
          |      },
          |      {
          |         "grossAmount": 6000.999,
          |         "taxYear": "2019-20"
          |      }
          |   ],
          |   "allOtherIncomeReceivedWhilstAbroad": [
          |      {
          |         "countryCode": "FRA",
          |         "amountBeforeTax": -1999.99,
          |         "taxTakenOff": -2.23,
          |         "specialWithholdingTax": 3.233,
          |         "foreignTaxCreditRelief": false,
          |         "taxableAmount": 4.233,
          |         "residentialFinancialCostAmount": -2999.999,
          |         "broughtFwdResidentialFinancialCostAmount": 1999.995
          |      },
          |      {
          |         "countryCode": "IND",
          |         "amountBeforeTax": -2999.99,
          |         "taxTakenOff": -3.23,
          |         "specialWithholdingTax": 4.234,
          |         "foreignTaxCreditRelief": true,
          |         "taxableAmount": -5.237,
          |         "residentialFinancialCostAmount": -3999.992,
          |         "broughtFwdResidentialFinancialCostAmount": 2999.9956
          |      }
          |   ],
          |   "overseasIncomeAndGains": {
          |      "gainAmount": 3000.993
          |   },
          |   "chargeableForeignBenefitsAndGifts": {
          |      "transactionBenefit": 1999.998,
          |      "protectedForeignIncomeSourceBenefit": -2999.99,
          |      "protectedForeignIncomeOnwardGift": 3999.111,
          |      "benefitReceivedAsASettler": -4999.999,
          |      "onwardGiftReceivedAsASettler": 5999.995
          |   },
          |   "omittedForeignIncome": {
          |      "amount": -4000.999
          |   }
          |}
         """.stripMargin
      )

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "overseasIncomeAndGains": {
          |      "gainAmount": "no"
          |   }
          |}
        """.stripMargin
      )

      val missingFieldRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "businessReceipts": [
          |     {
          |      "grossAmount": 100.11
          |     }
          |   ]
          |}
        """.stripMargin
      )

      val countryCodeError: MtdError = CountryCodeFormatError.copy(
        paths = Some(Seq(
          "/allOtherIncomeReceivedWhilstAbroad/0/countryCode",
          "/allOtherIncomeReceivedWhilstAbroad/1/countryCode"
        ))
      )

      val countryCodeRuleError: MtdError = CountryCodeRuleError.copy(
        paths = Some(Seq(
          "/allOtherIncomeReceivedWhilstAbroad/0/countryCode",
          "/allOtherIncomeReceivedWhilstAbroad/1/countryCode"
        ))
      )

      val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
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

      val nonValidRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq("/overseasIncomeAndGains/gainAmount"))
      )

      val missingFieldRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq("/businessReceipts/0/taxYear"))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, requestBody: JsValue, expectedStatus: Int,
                                expectedBody: MtdError, scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear
            override val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", validRequestBodyJson, BAD_REQUEST, NinoFormatError, None),
          ("AA123456A", "20177", validRequestBodyJson,  BAD_REQUEST, TaxYearFormatError, None),
          ("AA123456A", "2019-21", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "2018-19", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          ("AA123456A", "2019-20", invalidCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeError, None),
          ("AA123456A", "2019-20", ruleCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeRuleError, None),
          ("AA123456A", "2019-20", nonsenseRequestBody, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          ("AA123456A", "2019-20", allInvalidValueRequestBodyJson, BAD_REQUEST, allInvalidValueRequestError, None),
          ("AA123456A", "2019-20", nonValidRequestBodyJson, BAD_REQUEST, nonValidRequestBodyErrors, Some("(invalid request body format)")),
          ("AA123456A", "2019-20", missingFieldRequestBodyJson, BAD_REQUEST, missingFieldRequestBodyErrors, Some("(missing mandatory fields)"))
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.PUT, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "des message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
          (UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
