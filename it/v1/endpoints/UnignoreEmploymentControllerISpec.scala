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

import api.models.domain.TaxYear
import api.models.errors._
import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class UnignoreEmploymentControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String         = "AA123456A"
    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    val taxYear: String      = "2023-24"

    def mtdUri: String = s"/employments/$nino/$taxYear/$employmentId/unignore"

    def downstreamUri: String = s"/income-tax/${TaxYear.fromMtd(taxYear).asTysDownstream}/employments/$nino/ignore/$employmentId"

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links": [
         |      {
         |         "href": "/individuals/income-received/employments/$nino/$taxYear",
         |         "rel": "list-employments",
         |         "method": "GET"
         |      },
         |      {
         |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
         |         "rel": "self",
         |         "method": "GET"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123")
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

  "Calling the 'unignore employment' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override val taxYear: String = "2021-22"
        override val hateoasResponse: JsValue = Json.parse(
          s"""
             |{
             |   "links": [
             |      {
             |         "href": "/individuals/income-received/employments/$nino/$taxYear",
             |         "rel": "list-employments",
             |         "method": "GET"
             |      },
             |      {
             |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
             |         "rel": "self",
             |         "method": "GET"
             |      }
             |   ]
             |}
       """.stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().post(JsObject.empty))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request with a Tax Year Specific (TYS) tax year is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT)
        }

        override def request(): WSRequest =
          super.request().addHttpHeaders("suspend-temporal-validations" -> "true")

        val response: WSResponse = await(request().post(JsObject.empty))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a validation error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestTaxYear: String,
                              requestEmploymentId: String,
                              expectedStatus: Int,
                              expectedBody: MtdError,
                              scenario: Option[String]): Unit = {
        s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new Test {

          override val nino: String         = requestNino
          override val taxYear: String      = requestTaxYear
          override val employmentId: String = requestEmploymentId

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().post(JsObject.empty))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = List(
        ("AA1123A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, NinoFormatError, None),
        ("AA123456A", "20199", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, TaxYearFormatError, None),
        ("AA123456A", "2019-20", "ABCDE12345FG", BAD_REQUEST, EmploymentIdFormatError, None),
        ("AA123456A", "2018-19", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, RuleTaxYearNotSupportedError, None),
        ("AA123456A", "2019-21", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
        ("AA123456A", getCurrentTaxYear, "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, RuleTaxYearNotEndedError, None)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "return a service error according to spec" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamErrorCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamErrorCode error and status $downstreamStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamStatus, errorBody(downstreamErrorCode))
          }

          override def request(): WSRequest =
            super.request().addHttpHeaders("suspend-temporal-validations" -> "true")
          val response: WSResponse = await(request().post(JsObject.empty))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val errors = List(
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "INVALID_EMPLOYMENT_ID", BAD_REQUEST, EmploymentIdFormatError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (FORBIDDEN, "CUSTOMER_ADDED", BAD_REQUEST, RuleCustomEmploymentUnignoreError),
        (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "BEFORE_TAX_YEAR_ENDED", BAD_REQUEST, RuleTaxYearNotEndedError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )

      val extraTysErrors = List(
        (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
      )

      (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
    }

  }

}
