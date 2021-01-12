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

class AmendDividendsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
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

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
         |         "rel":"create-and-amend-dividends-income",
         |         "method":"PUT"
         |      },
         |      {
         |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
         |         "rel":"self",
         |         "method":"GET"
         |      },
         |      {
         |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
         |         "rel":"delete-dividends-income",
         |         "method":"DELETE"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    def uri: String = s"/dividends/$nino/$taxYear"

    def desUri: String = s"/income-tax/income/dividends/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'amend dividends' endpoint" should {
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

    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new Test {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
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

        val allInvalidValueRequestError: List[MtdError] = List(
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

        val iirDividendsIncomeAmendErrorsRequest: JsValue = Json.parse(
          """
            |{
            |   "foreignDividend": [
            |      {
            |        "countryCode": "GERMANY",
            |        "amountBeforeTax": 1232.223,
            |        "taxTakenOff": 22.22,
            |        "specialWithholdingTax": 27.35,
            |        "foreignTaxCreditRelief": true,
            |        "taxableAmount": 2321.22
            |      },
            |      {
            |        "countryCode": "PUR",
            |        "amountBeforeTax": 1350.55,
            |        "taxTakenOff": -25.27,
            |        "specialWithholdingTax": 30.59,
            |        "foreignTaxCreditRelief": false,
            |        "taxableAmount": 2500.99
            |      }
            |   ],
            |   "dividendIncomeReceivedWhilstAbroad": [
            |      {
            |        "countryCode": "FRANCE",
            |        "amountBeforeTax": 1232.22,
            |        "taxTakenOff": 22.22,
            |        "specialWithholdingTax": 27.358,
            |        "foreignTaxCreditRelief": true,
            |        "taxableAmount": 2321.22
            |      },
            |      {
            |        "countryCode": "SBT",
            |        "amountBeforeTax": 1350.55,
            |        "taxTakenOff": 25.27,
            |        "specialWithholdingTax": 30.59,
            |        "foreignTaxCreditRelief": false,
            |        "taxableAmount": -2500.99
            |       }
            |   ],
            |   "stockDividend": {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "grossAmount": 12321.22
            |   },
            |   "redeemableShares": {
            |      "customerReference": "my shares",
            |      "grossAmount": 12345.758
            |   },
            |   "bonusIssuesOfSecurities": {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "grossAmount": 12500.89
            |   },
            |   "closeCompanyLoansWrittenOff": {
            |      "customerReference": "write off",
            |      "grossAmount": -13700.55
            |   }
            |}
            |""".stripMargin)

        val iirDividendsIncomeAmendErrorsResponse: JsValue = Json.parse(
          """
            |{
            |   "code":"INVALID_REQUEST",
            |   "message":"Invalid request",
            |   "errors": [
            |        {
            |            "code": "RULE_COUNTRY_CODE",
            |            "message": "The country code is not a valid ISO 3166-1 alpha-3 country code",
            |            "paths": [
            |                "/foreignDividend/1/countryCode",
            |                "/dividendIncomeReceivedWhilstAbroad/1/countryCode"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_VALUE",
            |            "message": "The field should be between 0 and 99999999999.99",
            |            "paths": [
            |                "/foreignDividend/0/amountBeforeTax",
            |                "/foreignDividend/1/taxTakenOff",
            |                "/dividendIncomeReceivedWhilstAbroad/0/specialWithholdingTax",
            |                "/dividendIncomeReceivedWhilstAbroad/1/taxableAmount",
            |                "/redeemableShares/grossAmount",
            |                "/closeCompanyLoansWrittenOff/grossAmount"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_CUSTOMER_REF",
            |            "message": "The provided customer reference is invalid",
            |            "paths": [
            |                "/stockDividend/customerReference",
            |                "/bonusIssuesOfSecurities/customerReference"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_COUNTRY_CODE",
            |            "message": "The format of the country code is invalid",
            |            "paths": [
            |                "/foreignDividend/0/countryCode",
            |                "/dividendIncomeReceivedWhilstAbroad/0/countryCode"
            |            ]
            |        }
            |    ]
            |}
            |""".stripMargin)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(iirDividendsIncomeAmendErrorsRequest))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe iirDividendsIncomeAmendErrorsResponse
      }
    }

    "return error according to spec" when {

      val validRequestBodyJson: JsValue = Json.parse(
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

      val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignDividend": [
          |      {
          |        "countryCode": "GERMANY",
          |        "amountBeforeTax": 1232.22,
          |        "taxTakenOff": 22.22,
          |        "specialWithholdingTax": 27.35,
          |        "foreignTaxCreditRelief": true,
          |        "taxableAmount": 2321.22
          |      },
          |      {
          |        "countryCode": "FRANCE",
          |        "amountBeforeTax": 1350.55,
          |        "taxTakenOff": 25.27,
          |        "specialWithholdingTax": 30.59,
          |        "foreignTaxCreditRelief": false,
          |        "taxableAmount": 2500.99
          |      }
          |   ]
          |}
        """.stripMargin
      )

      val ruleCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "dividendIncomeReceivedWhilstAbroad": [
          |      {
          |        "countryCode": "PUR",
          |        "amountBeforeTax": 1232.22,
          |        "taxTakenOff": 22.22,
          |        "specialWithholdingTax": 27.35,
          |        "foreignTaxCreditRelief": true,
          |        "taxableAmount": 2321.22
          |      },
          |      {
          |        "countryCode": "SBT",
          |        "amountBeforeTax": 1350.55,
          |        "taxTakenOff": 25.27,
          |        "specialWithholdingTax": 30.59,
          |        "foreignTaxCreditRelief": false,
          |        "taxableAmount": 2500.99
          |       }
          |   ]
          |}
        """.stripMargin
      )

      val nonsenseRequestBody: JsValue = Json.parse(
        """
          |{
          |  "field": "value"
          |}
        """.stripMargin
      )

      val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
         """
          |{
          |   "stockDividend": {
          |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |      "grossAmount": 12321.22
          |   },
          |   "redeemableShares": {
          |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |      "grossAmount": 12345.75
          |   }
          |}
        """.stripMargin
      )

      val allInvalidValueRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignDividend": [
          |      {
          |        "countryCode": "DEU",
          |        "amountBeforeTax": -1232.22,
          |        "taxTakenOff": 22.223,
          |        "specialWithholdingTax": -27.35,
          |        "foreignTaxCreditRelief": true,
          |        "taxableAmount": 2321.224
          |      },
          |      {
          |        "countryCode": "FRA",
          |        "amountBeforeTax": -1350.55,
          |        "taxTakenOff": 25.275,
          |        "specialWithholdingTax": 30.596,
          |        "foreignTaxCreditRelief": false,
          |        "taxableAmount": 2500.997
          |      }
          |   ],
          |   "dividendIncomeReceivedWhilstAbroad": [
          |      {
          |        "countryCode": "DEU",
          |        "amountBeforeTax": -1232.22,
          |        "taxTakenOff": -22.22,
          |        "specialWithholdingTax": 27.353,
          |        "foreignTaxCreditRelief": true,
          |        "taxableAmount": 2321.223
          |      },
          |      {
          |        "countryCode": "FRA",
          |        "amountBeforeTax": -1350.55,
          |        "taxTakenOff": -25.27,
          |        "specialWithholdingTax": 30.594,
          |        "foreignTaxCreditRelief": false,
          |        "taxableAmount": 2500.998
          |       }
          |   ],
          |   "stockDividend": {
          |      "customerReference": "my divs",
          |      "grossAmount": -12321.22
          |   },
          |   "redeemableShares": {
          |      "customerReference": "my shares",
          |      "grossAmount": -12345.75
          |   },
          |   "bonusIssuesOfSecurities": {
          |      "customerReference": "my secs",
          |      "grossAmount": 12500.896
          |   },
          |   "closeCompanyLoansWrittenOff": {
          |      "customerReference": "write off",
          |      "grossAmount": 13700.557
          |   }
          |}
        """.stripMargin
      )

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignDividend": [
          |      {
          |        "countryCode": "DEU",
          |        "amountBeforeTax": 1232.22,
          |        "taxTakenOff": true,
          |        "specialWithholdingTax": 27.35,
          |        "foreignTaxCreditRelief": true,
          |        "taxableAmount": 2321.22
          |      },
          |      {
          |        "countryCode": "FRA",
          |        "amountBeforeTax": 1350.55,
          |        "taxTakenOff": false,
          |        "specialWithholdingTax": 30.59,
          |        "foreignTaxCreditRelief": false,
          |        "taxableAmount": 2500.99
          |      }
          |   ]
          |}
          """.stripMargin
      )

      val missingFieldRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "redeemableShares": {
          |      "customerReference": "my shares"
          |   }
          |}
          """.stripMargin
      )

      val countryCodeError: MtdError = CountryCodeFormatError.copy(
        paths = Some(Seq(
          "/foreignDividend/0/countryCode",
          "/foreignDividend/1/countryCode"
        ))
      )

      val countryCodeRuleError: MtdError = CountryCodeRuleError.copy(
        paths = Some(Seq(
          "/dividendIncomeReceivedWhilstAbroad/0/countryCode",
          "/dividendIncomeReceivedWhilstAbroad/1/countryCode"
        ))
      )

      val customerRefError: MtdError = CustomerRefFormatError.copy(
        paths = Some(Seq(
          "/stockDividend/customerReference",
          "/redeemableShares/customerReference"
        ))
      )

      val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
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
      )

      val nonValidRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(
        "/foreignDividend/0/taxTakenOff",
        "/foreignDividend/1/taxTakenOff"
      )))

      val missingFieldRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(
        "/redeemableShares/grossAmount"
      )))

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
          ("AA123456A", "2015-17", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "2018-19", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          ("AA123456A", "2019-20", invalidCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeError, None),
          ("AA123456A", "2019-20", ruleCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeRuleError, None),
          ("AA123456A", "2019-20", nonsenseRequestBody, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          ("AA123456A", "2019-20", invalidCustomerRefRequestBodyJson, BAD_REQUEST, customerRefError, None),
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
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}