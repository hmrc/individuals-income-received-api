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

import api.controllers.requestParsers.validators.validations.DecimalValueValidation.ZERO_MINIMUM_INCLUSIVE
import api.models.errors._
import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.{IntegrationBaseSpec, WireMockMethods}

class CreateAmendUkDividendsAnnualSummaryControllerISpec extends IntegrationBaseSpec with WireMockMethods {

  val requestJson: JsValue =
    Json.parse("""{
                  |   "ukDividends": 10.99,
                  |   "otherUkDividends": 12.99
                  |}""".stripMargin)

  val downstreamRequestBodyJson: JsValue =
    Json.parse("""{
                 |   "ukDividends": 10.99,
                 |   "otherUkDividends": 12.99
                 |}""".stripMargin)

  "Calling the endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub
            .when(method = DownstreamStub.POST, uri = downstreamUri)
            .withRequestBody(downstreamRequestBodyJson)
            .thenReturn(status = OK, JsObject.empty)
        }

        val response: WSResponse = await(mtdRequest.put(requestJson))
        response.status shouldBe OK
        response.json shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a valid request is made for a Tax Year Specific tax year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub
            .when(method = DownstreamStub.POST, uri = downstreamUri)
            .withRequestBody(downstreamRequestBodyJson)
            .thenReturn(status = OK, JsObject.empty)
        }

        val response: WSResponse = await(mtdRequest.put(requestJson))
        response.status shouldBe OK
        response.json shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedError: MtdError): Unit = {

          s"validation fails with ${expectedError.code} error" in new NonTysTest {
            override def nino: String    = requestNino
            override def taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(mtdRequest.put(requestBody))
            response.json shouldBe Json.toJson(expectedError)
            response.status shouldBe expectedStatus
          }
        }

        val input = Seq(
          ("BAD_NINO", "2020-21", requestJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "BAD_TAX_YEAR", requestJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2020-22", requestJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2016-17", requestJson, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2020-21", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError),
          (
            "AA123456A",
            "2020-21",
            Json.parse("""{ "ukDividends": -10.99 }"""),
            BAD_REQUEST,
            ValueFormatError.copy(message = ZERO_MINIMUM_INCLUSIVE, paths = Some(Seq("/ukDividends"))))
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedError: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.POST, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(mtdRequest.put(requestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedError)
            response.header("Content-Type") shouldBe Some("application/json")
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
          (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAXYEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_TYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (FORBIDDEN, "NOT_FOUND_INCOME_SOURCE", NOT_FOUND, NotFoundError),
          (FORBIDDEN, "MISSING_CHARITIES_NAME_GIFT_AID", INTERNAL_SERVER_ERROR, InternalError),
          (FORBIDDEN, "MISSING_GIFT_AID_AMOUNT", INTERNAL_SERVER_ERROR, InternalError),
          (FORBIDDEN, "MISSING_CHARITIES_NAME_INVESTMENT", INTERNAL_SERVER_ERROR, InternalError),
          (FORBIDDEN, "MISSING_INVESTMENT_AMOUNT", INTERNAL_SERVER_ERROR, InternalError),
          (FORBIDDEN, "INVALID_ACCOUNTING_PERIOD", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (GONE, "GONE", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCE_TYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (NOT_FOUND, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "INCOMPATIBLE_INCOME_SOURCE", INTERNAL_SERVER_ERROR, InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    def taxYear: String
    def downstreamTaxYear: String
    def downstreamUri: String

    def nino: String = "AA123456A"

    val hateoasResponse: JsValue = Json.parse(s"""
      |{
      |  "links":[
      |      {
      |         "href":"/individuals/income-received/uk-dividends/$nino/$taxYear",
      |         "method":"PUT",
      |         "rel":"create-and-amend-uk-dividends-income"
      |      },
      |      {
      |         "href":"/individuals/income-received/uk-dividends/$nino/$taxYear",
      |         "method":"GET",
      |         "rel":"self"
      |      },
      |      {
      |         "href":"/individuals/income-received/uk-dividends/$nino/$taxYear",
      |         "method":"DELETE",
      |         "rel":"delete-uk-dividends-income"
      |      }
      |   ]
      |}""".stripMargin)

    def setupStubs(): StubMapping

    def mtdRequest: WSRequest = {
      setupStubs()
      buildRequest(s"/uk-dividends/$nino/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  private trait NonTysTest extends Test {
    def taxYear: String           = "2020-21"
    def downstreamTaxYear: String = "2021"
    def downstreamUri: String     = s"/income-tax/nino/$nino/income-source/dividends/annual/$downstreamTaxYear"
  }

  private trait TysIfsTest extends Test {
    def taxYear: String           = "2023-24"
    def downstreamTaxYear: String = "23-24"
    def downstreamUri: String     = s"/income-tax/$downstreamTaxYear/$nino/income-source/dividends/annual"
  }

}
