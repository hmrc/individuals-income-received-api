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

class AmendForeignControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |   "foreignEarnings": {
        |      "customerReference": "FOREIGNINCME123A",
        |      "earningsNotTaxableUK": 1999.99
        |   },
        |   "unremittableForeignIncome": [
        |       {
        |          "countryCode": "FRA",
        |          "amountInForeignCurrency": 1999.99,
        |          "amountTaxPaid": 1999.99
        |       },
        |       {
        |          "countryCode": "IND",
        |          "amountInForeignCurrency": 2999.99,
        |          "amountTaxPaid": 2999.99
        |       }
        |    ]
        |}
      """.stripMargin
    )

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/foreign/$nino/$taxYear",
         |         "rel":"create-and-amend-foreign-income",
         |         "method":"PUT"
         |      },
         |      {
         |         "href":"/individuals/income-received/foreign/$nino/$taxYear",
         |         "rel":"self",
         |         "method":"GET"
         |      },
         |      {
         |         "href":"/individuals/income-received/foreign/$nino/$taxYear",
         |         "rel":"delete-foreign-income",
         |         "method":"DELETE"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    def uri: String = s"/foreign/$nino/$taxYear"

    def desUri: String = s"/income-tax/income/foreign/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'amend foreign' endpoint" should {
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
            |   "foreignEarnings": {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "earningsNotTaxableUK": 999999999991.99
            |   },
            |   "unremittableForeignIncome": [
            |       {
            |          "countryCode": "Belgium",
            |          "amountInForeignCurrency": 999999999991.13,
            |          "amountTaxPaid": -200.50
            |       },
            |       {
            |          "countryCode": "PUR",
            |          "amountInForeignCurrency": -500.123,
            |          "amountTaxPaid": -600.500
            |       }
            |    ]
            |}
          """.stripMargin
        )

        val allInvalidValueRequestError: List[MtdError] = List(
          CountryCodeRuleError.copy(
            paths = Some(Seq("/unremittableForeignIncome/1/countryCode"))
          ),
          ValueFormatError.copy(
            paths = Some(List(
              "/foreignEarnings/earningsNotTaxableUK",
              "/unremittableForeignIncome/0/amountInForeignCurrency",
              "/unremittableForeignIncome/0/amountTaxPaid",
              "/unremittableForeignIncome/1/amountInForeignCurrency",
              "/unremittableForeignIncome/1/amountTaxPaid"
            )),
            message = "The field should be between 0 and 99999999999.99"
          ),
          CustomerRefFormatError.copy(
            paths = Some(List("/foreignEarnings/customerReference"))
          ),
          CountryCodeFormatError.copy(
            paths = Some(Seq("/unremittableForeignIncome/0/countryCode"))
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
    }

    "return error according to spec" when {

      val validRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignEarnings": {
          |      "customerReference": "FOREIGNINCME123A",
          |      "earningsNotTaxableUK": 1999.99
          |   },
          |   "unremittableForeignIncome": [
          |       {
          |          "countryCode": "FRA",
          |          "amountInForeignCurrency": 1999.99,
          |          "amountTaxPaid": 1999.99
          |       },
          |       {
          |          "countryCode": "IND",
          |          "amountInForeignCurrency": 2999.99,
          |          "amountTaxPaid": 2999.99
          |       }
          |    ]
          |}
        """.stripMargin
      )

      val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "unremittableForeignIncome": [
          |       {
          |          "countryCode": "Belgium",
          |          "amountInForeignCurrency": 1999.99,
          |          "amountTaxPaid": 1999.99
          |       },
          |       {
          |          "countryCode": "notACountryCode",
          |          "amountInForeignCurrency": 2999.99,
          |          "amountTaxPaid": 2999.99
          |       }
          |    ]
          |}
        """.stripMargin
      )

      val ruleCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "unremittableForeignIncome": [
          |       {
          |          "countryCode": "SPR",
          |          "amountInForeignCurrency": 1999.99,
          |          "amountTaxPaid": 1999.99
          |       },
          |       {
          |          "countryCode": "PUR",
          |          "amountInForeignCurrency": 2999.99,
          |          "amountTaxPaid": 2999.99
          |       }
          |    ]
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
          |   "foreignEarnings": {
          |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |      "earningsNotTaxableUK": 1999.99
          |   }
          |}
        """.stripMargin
      )

      val allInvalidValueRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignEarnings": {
          |      "customerReference": "FOREIGNINCME123A",
          |      "earningsNotTaxableUK": 999999999991.99
          |   },
          |   "unremittableForeignIncome": [
          |       {
          |          "countryCode": "ITA",
          |          "amountInForeignCurrency": 999999999991.13,
          |          "amountTaxPaid": -200.50
          |       },
          |       {
          |          "countryCode": "NGA",
          |          "amountInForeignCurrency": -500.123,
          |          "amountTaxPaid": -600.500
          |       }
          |    ]
          |}
        """.stripMargin
      )

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignEarnings": {
          |     "customerReference":"FOREIGNINCME123A",
          |     "earningsNotTaxableUK":"99999999999.99"
          |   },
          |   "unremittableForeignIncome": [
          |     {
          |       "countryCode":"FRA",
          |       "amountInForeignCurrency":"0",
          |       "amountTaxPaid":true
          |     },
          |     {
          |       "countryCode":"GBR",
          |       "amountInForeignCurrency":"99999999999.99",
          |       "amountTaxPaid":false
          |     },
          |     {
          |       "countryCode":"ESP",
          |       "amountInForeignCurrency":"0.99",
          |       "amountTaxPaid":"100"
          |     }
          |   ]
          |}
        """.stripMargin
      )

      val missingFieldRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "unremittableForeignIncome": [
          |       {
          |          "amountInForeignCurrency": 1999.99,
          |          "amountTaxPaid": 1999.99
          |       }
          |    ]
          |}
        """.stripMargin
      )

      val countryCodeError: MtdError = CountryCodeFormatError.copy(
        paths = Some(Seq(
        "/unremittableForeignIncome/0/countryCode",
        "/unremittableForeignIncome/1/countryCode"
        ))
      )

      val countryCodeRuleError: MtdError = CountryCodeRuleError.copy(
        paths = Some(Seq(
          "/unremittableForeignIncome/0/countryCode",
          "/unremittableForeignIncome/1/countryCode"
        ))
      )

      val customerRefError = CustomerRefFormatError.copy(
        paths = Some(Seq(
          "/foreignEarnings/customerReference"
        ))
      )

      val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
        message = "The field should be between 0 and 99999999999.99",
        paths = Some(List(
          "/foreignEarnings/earningsNotTaxableUK",
          "/unremittableForeignIncome/0/amountInForeignCurrency",
          "/unremittableForeignIncome/0/amountTaxPaid",
          "/unremittableForeignIncome/1/amountInForeignCurrency",
          "/unremittableForeignIncome/1/amountTaxPaid"
        ))
      )

      val nonValidRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq(
        "/unremittableForeignIncome/0/amountTaxPaid",
        "/unremittableForeignIncome/1/amountTaxPaid"
        ))
      )

      val missingFieldRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq(
        "/unremittableForeignIncome/0/countryCode"
        ))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String,requestTaxYear: String, requestBody: JsValue, expectedStatus: Int,
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

        val validNino = "AA123456A"
        val validTaxYear = "2019-20"

        val input = Seq(
          ("AA1123A", validTaxYear, validRequestBodyJson, BAD_REQUEST, NinoFormatError, None),
          (validNino, "20177", validRequestBodyJson,  BAD_REQUEST, TaxYearFormatError, None),
          (validNino, "2018-20", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          (validNino, "2018-19", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          (validNino, validTaxYear, invalidCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeError, None),
          (validNino, validTaxYear, ruleCountryCodeRequestBodyJson, BAD_REQUEST, countryCodeRuleError, None),
          (validNino, validTaxYear, nonsenseRequestBody, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          (validNino, validTaxYear, invalidCustomerRefRequestBodyJson, BAD_REQUEST, customerRefError, None),
          (validNino, validTaxYear, allInvalidValueRequestBodyJson, BAD_REQUEST, allInvalidValueRequestError, None),
          (validNino, validTaxYear, nonValidRequestBodyJson, BAD_REQUEST, nonValidRequestBodyErrors, Some("(invalid request body format)")),
          (validNino, validTaxYear, missingFieldRequestBodyJson, BAD_REQUEST, missingFieldRequestBodyErrors, Some("(missing mandatory fields)"))
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