/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.http.Status.{BAD_REQUEST, CONFLICT, FORBIDDEN, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{ACCEPT, AUTHORIZATION}
import support.IntegrationBaseSpec

class AddUkSavingsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val savingsAccountId: String = "SAVKB2UVwUTBQGJ"
    val taxYear: String = "2020-21"


    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |   "accountName": "Shares savings account"
        |}
        |""".stripMargin)

    val responseJson: JsValue = Json.parse(
      s"""
         |{
         |    "savingsAccountId": "$savingsAccountId"
         |    "links":[
         |      {
         |         "href":"/individuals/income-received/savings/uk-accounts/$nino",
         |         "method":"GET",
         |         "rel":"list-all-uk-savings-account"
         |      },
         |      {
         |         "href":"/individuals/income-received/savings/uk-accounts/$nino/$taxYear/$savingsAccountId",
         |         "method":"PUT",
         |         "rel":"create-and-amend-uk-savings-account-annual-summary"
         |      }
         |   ]
         |}
         |""".stripMargin)

    def uri: String = s"/savings/uk-accounts/$nino"

    def ifsUri: String = s"/income-tax/income-sources/nino/$nino"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }
  }

  "calling the 'add uk savings' endpoint" should {
    "return a 200 status code" when {
      "any valid requst is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, ifsUri, OK, responseJson)
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe responseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      val validRequestJson: JsValue = Json.parse(
        """
          |{
          |   "accountName": "Shares savings account"
          |}
          |""".stripMargin)

      val emptyRequestJson: JsValue = JsObject.empty

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "accountName": "Shares savings account!"
          |}
          |""".stripMargin)

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear
            override val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", validRequestJson, BAD_REQUEST, NinoFormatError, None),
          ("AA123456A", "2019-20", emptyRequestJson, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          ("AA123456A", "2019-20", nonValidRequestBodyJson, BAD_REQUEST, AccountNameFormatError, None)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "ifs service error" when  {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.POST, ifsUri, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().post(requestBodyJson))
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
          (BAD_REQUEST, "INVALID_IDVALUE", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (CONFLICT, "MAX_ACCOUNTS_REACHED", FORBIDDEN, RuleMaximumSavingsAccoountsLimit),
          (CONFLICT, "ALREADY_EXISTS", FORBIDDEN, RuleDuplicateAccountName),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
