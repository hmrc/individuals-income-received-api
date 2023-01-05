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

package v1.endpoints

import api.models.errors._
import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class AmendDividendsControllerISpec extends IntegrationBaseSpec {

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

  private trait Test {

    val nino: String = "AA123456A"
    def taxYear: String

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

    def downstreamUri: String

    def setupStubs(): Unit = ()

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/dividends/$nino/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  private class NonTysTest extends Test {
    def taxYear: String       = "2019-20"
    def downstreamUri: String = s"/income-tax/income/dividends/$nino/2019-20"
  }

  private class TysTest extends Test {
    def taxYear: String       = "2023-24"
    def downstreamUri: String = s"/income-tax/income/dividends/23-24/$nino"
  }

  "Calling the 'amend dividends' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made (TYS)" in new TysTest {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignDividend": [
          |      {
          |        "countryCode": "GERMANY",
          |        "foreignTaxCreditRelief": true,
          |        "taxableAmount": 2321.22
          |      },
          |      {
          |        "countryCode": "FRANCE",
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
          |        "foreignTaxCreditRelief": true,
          |        "taxableAmount": 2321.22
          |      },
          |      {
          |        "countryCode": "SBT",
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
          |   "stockDividend": {
          |      "customerReference": "my divs",
          |      "grossAmount": -12321.22
          |   }
          |}
        """.stripMargin
      )

      val countryCodeError: MtdError = CountryCodeFormatError.copy(
        paths = Some(
          Seq(
            "/foreignDividend/0/countryCode",
            "/foreignDividend/1/countryCode"
          ))
      )

      val countryCodeRuleError: MtdError = CountryCodeRuleError.copy(
        paths = Some(
          Seq(
            "/dividendIncomeReceivedWhilstAbroad/0/countryCode",
            "/dividendIncomeReceivedWhilstAbroad/1/countryCode"
          ))
      )

      val customerRefError: MtdError = CustomerRefFormatError.copy(
        paths = Some(
          Seq(
            "/stockDividend/customerReference",
            "/redeemableShares/customerReference"
          ))
      )

      val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
        message = "The value must be between 0 and 99999999999.99",
        paths = Some(
          List(
            "/stockDividend/grossAmount"
          ))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            val response: WSResponse = await(request().put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", requestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", requestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2015-17", requestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2018-19", requestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2019-20", invalidCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeError),
          ("AA123456A", "2019-20", ruleCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeRuleError),
          ("AA123456A", "2019-20", nonsenseRequestBody, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          ("AA123456A", "2019-20", invalidCustomerRefRequestBodyJson, BAD_REQUEST, customerRefError),
          ("AA123456A", "2019-20", allInvalidValueRequestBodyJson, BAD_REQUEST, allInvalidValueRequestError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "message"
             |}
            """.stripMargin

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
