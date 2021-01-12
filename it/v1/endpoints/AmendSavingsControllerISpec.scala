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

class AmendSavingsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "securities":
        |      {
        |        "taxTakenOff": 100.11,
        |        "grossAmount": 100.22,
        |        "netAmount": 100.33
        |      },
        |  "foreignInterest":   [
        |     {
        |        "amountBeforeTax": 101.11,
        |        "countryCode": "FRA",
        |        "taxTakenOff": 102.22,
        |        "specialWithholdingTax": 103.33,
        |        "taxableAmount": 104.44,
        |        "foreignTaxCreditRelief": true
        |      },
        |      {
        |        "amountBeforeTax": 201.11,
        |        "countryCode": "DEU",
        |        "taxTakenOff": 202.22,
        |        "specialWithholdingTax": 203.33,
        |        "taxableAmount": 204.44,
        |        "foreignTaxCreditRelief": true
        |      }
        |   ]
        |}
    """.stripMargin
    )

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/savings/$nino/$taxYear",
         |         "rel":"create-and-amend-savings-income",
         |         "method":"PUT"
         |      },
         |      {
         |         "href":"/individuals/income-received/savings/$nino/$taxYear",
         |         "rel":"self",
         |         "method":"GET"
         |      },
         |      {
         |         "href":"/individuals/income-received/savings/$nino/$taxYear",
         |         "rel":"delete-savings-income",
         |         "method":"DELETE"
         |      }
         |   ]
         |}
    """.stripMargin
    )

    def uri: String = s"/savings/$nino/$taxYear"

    def desUri: String = s"/income-tax/income/savings/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'amend savings' endpoint" should {
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
            |   "securities": {
            |      "taxTakenOff": 100.111,
            |      "grossAmount": -100.12,
            |      "netAmount": 999999999991.99
            |   },
            |   "foreignInterest": [
            |       {
            |          "amountBeforeTax": -200.11,
            |          "countryCode": "SKEGNESS",
            |          "taxTakenOff": 200.121,
            |          "specialWithholdingTax": 999999999991.13,
            |          "taxableAmount": -200.14,
            |          "foreignTaxCreditRelief": false
            |       },
            |       {
            |          "amountBeforeTax": -300.11,
            |          "countryCode": "SKEG_",
            |          "taxTakenOff": -300.100,
            |          "specialWithholdingTax": -300.134,
            |          "taxableAmount": -300.14,
            |          "foreignTaxCreditRelief": true
            |       },
            |       {
            |          "amountBeforeTax": -300.11,
            |          "countryCode": "FRE",
            |          "taxTakenOff": -300.100,
            |          "specialWithholdingTax": -300.134,
            |          "taxableAmount": -300.14,
            |          "foreignTaxCreditRelief": true
            |       }
            |    ]
            |}
          """.stripMargin
        )

        val allInvalidValueRequestError: List[MtdError] = List(
          CountryCodeRuleError.copy(
            paths = Some(List(
              "/foreignInterest/2/countryCode"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(List(
              "/securities/taxTakenOff",
              "/securities/grossAmount",
              "/securities/netAmount",
              "/foreignInterest/0/amountBeforeTax",
              "/foreignInterest/0/taxTakenOff",
              "/foreignInterest/0/specialWithholdingTax",
              "/foreignInterest/0/taxableAmount",
              "/foreignInterest/1/amountBeforeTax",
              "/foreignInterest/1/taxTakenOff",
              "/foreignInterest/1/specialWithholdingTax",
              "/foreignInterest/1/taxableAmount",
              "/foreignInterest/2/amountBeforeTax",
              "/foreignInterest/2/taxTakenOff",
              "/foreignInterest/2/specialWithholdingTax",
              "/foreignInterest/2/taxableAmount"
            ))
          ),
          CountryCodeFormatError.copy(
            paths = Some(List(
              "/foreignInterest/0/countryCode",
              "/foreignInterest/1/countryCode"
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
    }

    "return error according to spec" when {

      val validRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "securities": {
          |        "taxTakenOff": 100.11,
          |        "grossAmount": 100.22,
          |        "netAmount": 100.33
          |      },
          |  "foreignInterest": [
          |     {
          |        "amountBeforeTax": 101.11,
          |        "countryCode": "FRA",
          |        "taxTakenOff": 102.22,
          |        "specialWithholdingTax": 103.33,
          |        "taxableAmount": 104.44,
          |        "foreignTaxCreditRelief": true
          |      },
          |      {
          |        "amountBeforeTax": 201.11,
          |        "countryCode": "DEU",
          |        "taxTakenOff": 202.22,
          |        "specialWithholdingTax": 203.33,
          |        "taxableAmount": 204.44,
          |        "foreignTaxCreditRelief": true
          |      }
          |   ]
          |}
        """.stripMargin
      )

      val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "foreignInterest": [
          |     {
          |        "countryCode": "notACountryCode",
          |        "taxableAmount": 104.44,
          |        "foreignTaxCreditRelief": true
          |      },
          |      {
          |        "countryCode": "notACountryCode",
          |        "taxableAmount": 204.44,
          |        "foreignTaxCreditRelief": true
          |      }
          |   ]
          |}
        """.stripMargin
      )

      val ruleCountryCodeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "foreignInterest": [
          |     {
          |        "countryCode": "FRE",
          |        "taxableAmount": 104.44,
          |        "foreignTaxCreditRelief": true
          |      },
          |      {
          |        "countryCode": "ENL",
          |        "taxableAmount": 204.44,
          |        "foreignTaxCreditRelief": true
          |      }
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

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "securities": {
          |      "taxTakenOff": "no",
          |      "grossAmount": 100.12,
          |      "netAmount": 100.13
          |   },
          |   "foreignInterest": [
          |     {
          |       "countryCode": "DEU",
          |       "foreignTaxCreditRelief": 100,
          |       "taxableAmount": 200.33
          |     }
          |   ]
          |}
        """.stripMargin
      )

      val missingFieldRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "foreignInterest": [
          |     {
          |       "countryCode": "DEU",
          |       "foreignTaxCreditRelief": true
          |     }
          |   ]
          |}
        """.stripMargin
      )

      val countryCodeError: MtdError = CountryCodeFormatError.copy(
        paths = Some(Seq(
        "/foreignInterest/0/countryCode",
        "/foreignInterest/1/countryCode"
        ))
      )

      val countryCodeRuleError: MtdError = CountryCodeRuleError.copy(
        paths = Some(Seq(
          "/foreignInterest/0/countryCode",
          "/foreignInterest/1/countryCode"
        ))
      )

      val allInvalidValueRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "securities": {
          |      "taxTakenOff": 100.111,
          |      "grossAmount": -100.12,
          |      "netAmount": 999999999991.99
          |   },
          |   "foreignInterest": [
          |       {
          |          "amountBeforeTax": -200.11,
          |          "countryCode": "GBR",
          |          "taxTakenOff": 200.121,
          |          "specialWithholdingTax": 999999999991.13,
          |          "taxableAmount": -200.14,
          |          "foreignTaxCreditRelief": false
          |       },
          |       {
          |          "amountBeforeTax": -300.11,
          |          "countryCode": "GBR",
          |          "taxTakenOff": -300.100,
          |          "specialWithholdingTax": -300.134,
          |          "taxableAmount": -300.14,
          |          "foreignTaxCreditRelief": true
          |       }
          |    ]
          |}
    """.stripMargin
      )

      val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
        message = "The field should be between 0 and 99999999999.99",
        paths = Some(List(
          "/securities/taxTakenOff",
          "/securities/grossAmount",
          "/securities/netAmount",
          "/foreignInterest/0/amountBeforeTax",
          "/foreignInterest/0/taxTakenOff",
          "/foreignInterest/0/specialWithholdingTax",
          "/foreignInterest/0/taxableAmount",
          "/foreignInterest/1/amountBeforeTax",
          "/foreignInterest/1/taxTakenOff",
          "/foreignInterest/1/specialWithholdingTax",
          "/foreignInterest/1/taxableAmount"
        ))
      )

      val nonValidRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq(
          "/securities/taxTakenOff",
          "/foreignInterest/0/foreignTaxCreditRelief")
        )
      )

      val missingFieldRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq("/foreignInterest/0/taxableAmount"))
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
          ("AA123456A", "2015-17", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
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
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}