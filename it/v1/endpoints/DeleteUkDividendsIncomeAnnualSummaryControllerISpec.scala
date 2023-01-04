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
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class DeleteUkDividendsIncomeAnnualSummaryControllerISpec extends IntegrationBaseSpec {

  "Calling the 'delete uk dividends income' endpoint" should {
    "return a 204 status code" when {
      "any valid request is made" in new NonTysTest with Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().delete)
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request with a Tax Year Specific (TYS) tax year is made" in new TysIfsTest with Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().delete)
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest with Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().delete)
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2015-17", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2015-16", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "service error" when {
        "downstream returns a 200 response" in new NonTysTest with Test {
          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, OK)
          }

          val response: WSResponse = await(request().delete)
          response.status shouldBe INTERNAL_SERVER_ERROR
          response.json shouldBe Json.toJson(StandardDownstreamError)
          response.header("Content-Type") shouldBe Some("application/json")
        }

        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest with Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.POST, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().delete)
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val errors = Seq(
          (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TYPE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_TAXYEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (FORBIDDEN, "NOT_FOUND_INCOME_SOURCE", NOT_FOUND, NotFoundError),
          (FORBIDDEN, "MISSING_CHARITIES_NAME_GIFT_AID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (FORBIDDEN, "MISSING_GIFT_AID_AMOUNT", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (FORBIDDEN, "MISSING_CHARITIES_NAME_INVESTMENT", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (FORBIDDEN, "MISSING_INVESTMENT_AMOUNT", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (FORBIDDEN, "INVALID_ACCOUNTING_PERIOD", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (GONE, "GONE", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, StandardDownstreamError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_INCOMESOURCE_TYPE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (NOT_FOUND, "INCOME_SOURCE_DATA_NOT_FOUND", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "PERIOD_NOT_FOUND", NOT_FOUND, NotFoundError),
          (GONE, "PERIOD_ALREADY_DELETED", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String = "AA123456A"

    def taxYear: String
    def mtdUri: String = s"/uk-dividends/$nino/$taxYear"
    def downstreamUri: String

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "error message"
         |}
            """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String       = "2021-22"
    def downstreamUri: String = s"/income-tax/nino/$nino/income-source/dividends/annual/2022"
  }

  private trait TysIfsTest extends Test {
    def taxYear: String       = "2023-24"
    def downstreamUri: String = s"/income-tax/23-24/$nino/income-source/dividends/annual"
  }

}
