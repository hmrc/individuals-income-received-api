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

package v1r6.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1r6.models.errors._
import v1r6.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class UnignoreEmploymentControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"
    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

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

    def uri: String = s"/employments/$nino/$taxYear/$employmentId/unignore"

    def ifsUri: String = s"/income-tax/employments/$nino/$taxYear/ignore/$employmentId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'unignore employment' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, ifsUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().post(JsObject.empty))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, requestEmploymentId: String,
                                expectedStatus: Int, expectedBody: MtdError, scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear
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

        val input = Seq(
          ("AA1123A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, NinoFormatError, None),
          ("AA123456A", "20199", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, TaxYearFormatError, None),
          ("AA123456A", "2019-20", "ABCDE12345FG", BAD_REQUEST, EmploymentIdFormatError, None),
          ("AA123456A", "2018-19", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          ("AA123456A", "2019-21", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "2021-22", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", BAD_REQUEST, RuleTaxYearNotEndedError, None)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.DELETE, ifsUri, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().post(JsObject.empty))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "ifs message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_EMPLOYMENT_ID", BAD_REQUEST, EmploymentIdFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (FORBIDDEN, "CUSTOMER_ADDED", FORBIDDEN, RuleCustomEmploymentUnignoreError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "BEFORE_TAX_YEAR_ENDED", BAD_REQUEST, RuleTaxYearNotEndedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}