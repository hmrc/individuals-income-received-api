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
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class RetrieveUkSavingsAccountAnnualSummaryControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "TC663795B"
    def taxYear: String

    val savingsAccountId: String = "122784545874145"

    val downstreamResponse: JsValue = Json.parse("""
        |   {
        |     "savingsInterestAnnualIncome": [
        |      {
        |       "incomeSourceId": "122784545874145",
        |       "taxedUkInterest": 93556675358.99,
        |       "untaxedUkInterest": 34514974058.99
        |      }
        |     ]
        |   }
        |""".stripMargin)

    val mtdResponse: JsValue = Json.parse(s"""
        {
         |   "taxedUkInterest": 93556675358.99,
         |   "untaxedUkInterest": 34514974058.99,
         |   "links":[
         |     {
         |          "href":"/individuals/income-received/savings/uk-accounts/$nino/$taxYear/$savingsAccountId",
         |          "rel":"create-and-amend-uk-savings-account-annual-summary",
         |          "method":"PUT"
         |    },
         |    {
         |          "href":"/individuals/income-received/savings/uk-accounts/$nino/$taxYear/$savingsAccountId",
         |          "rel":"self",
         |          "method":"GET"
         |    }
         |   ]
         |}
         |""".stripMargin)

    def downstreamUri: String

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(s"/savings/uk-accounts/$nino/$taxYear/$savingsAccountId")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

  private trait NonTysTest extends Test {
    def taxYear: String = "2020-21"

    def downstreamUri: String = s"/income-tax/nino/$nino/income-source/savings/annual/2021"
  }

  private trait TysIfsTest extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/23-24/$nino/income-source/savings/annual"
  }

  "Calling the 'retrieve savings' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)

          DownstreamStub.onSuccess(
            method = DownstreamStub.GET,
            uri = downstreamUri,
            queryParams = Map[String, String]("incomeSourceId" -> savingsAccountId),
            status = OK,
            body = downstreamResponse)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made for a tax-year specific endpoint" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)

          DownstreamStub.onSuccess(
            method = DownstreamStub.GET,
            uri = downstreamUri,
            queryParams = Map[String, String]("incomeSourceId" -> savingsAccountId),
            status = OK,
            body = downstreamResponse)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestSavingsAccountId: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String             = requestNino
            override val taxYear: String          = requestTaxYear
            override val savingsAccountId: String = requestSavingsAccountId
            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request.get)
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", "AB1DE0123456789", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", "AB1DE0123456789", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2017-18", "AB1DE", BAD_REQUEST, SavingsAccountIdFormatError),
          ("AA123456A", "2015-17", "AB1DE0123456789", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2010-11", "AB1DE0123456789", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request.get)
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
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
          (BAD_REQUEST, "INVALID_INCOME_SOURCE", BAD_REQUEST, SavingsAccountIdFormatError),
          (NOT_FOUND, "NOT_FOUND_PERIOD", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "NOT_FOUND_INCOME_SOURCE", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, SavingsAccountIdFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCE_TYPE", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "SUBMISSION_PERIOD_NOT_FOUND", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "INCOME_DATA_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
