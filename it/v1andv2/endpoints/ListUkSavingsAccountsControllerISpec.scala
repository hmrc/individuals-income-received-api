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

package v1andv2.endpoints

import api.models.errors._
import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class ListUkSavingsAccountsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String             = "AA123456A"
    val savingsAccountId: String = "SAVKB2UVwUTBQGJ"

    val desResponse: JsValue = Json.parse(
      """
        |[
        |   {
        |      "incomeSourceId": "000000000000001",
        |      "incomeSourceName": "Bank Account 1",
        |      "identifier": "AA111111A",
        |      "incomeSourceType": "interest-from-uk-banks"
        |   },
        |   {
        |      "incomeSourceId": "000000000000002",
        |      "incomeSourceName": "Bank Account 2",
        |      "identifier": "AA111111A",
        |      "incomeSourceType": "interest-from-uk-banks"
        |   },
        |   {
        |      "incomeSourceId": "000000000000003",
        |      "incomeSourceName": "Bank Account 3",
        |      "identifier": "AA111111A",
        |      "incomeSourceType": "interest-from-uk-banks"
        |   }
        |]
    """.stripMargin
    )

    val mtdResponse: JsValue = Json.parse(s"""|{
          | "savingsAccounts":
          |  [
          |    {
          |        "savingsAccountId": "000000000000001",
          |        "accountName": "Bank Account 1"
          |    },
          |    {
          |        "savingsAccountId": "000000000000002",
          |        "accountName": "Bank Account 2"
          |    },
          |    {
          |        "savingsAccountId": "000000000000003",
          |        "accountName": "Bank Account 3"
          |    }
          | ],
          | "links": [
          |      {
          |         "href":"/individuals/income-received/savings/uk-accounts/$nino",
          |         "rel":"add-uk-savings-account",
          |         "method":"POST"
          |      },
          |      {
          |         "href":"/individuals/income-received/savings/uk-accounts/$nino",
          |         "rel":"self",
          |         "method":"GET"
          |      }
          | ]
          |}""".stripMargin)

    def uri: String = s"/savings/uk-accounts/$nino"

    def desUri: String = s"/income-tax/income-sources/nino/$nino"

    def queryParams: Seq[(String, String)] =
      Seq("savingsAccountId" -> Some(savingsAccountId))
        .collect { case (k, Some(v)) =>
          (k, v)
        }

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the 'list UK Savings Accounts' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, desUri, Map("incomeSourceId" -> savingsAccountId), OK, desResponse)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestSavingsAccountId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String             = requestNino
            override val savingsAccountId: String = requestSavingsAccountId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "SAVKB2UVwUTBQGJ", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "SAVKB2UVwUTBQG", BAD_REQUEST, SavingsAccountIdFormatError)
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
              DownstreamStub.onError(DownstreamStub.GET, desUri, Map("incomeSourceId" -> savingsAccountId), desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
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
          (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_IDVALUE", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_INCOMESOURCETYPE", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_TAXYEAR", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, SavingsAccountIdFormatError),
          (BAD_REQUEST, "INVALID_ENDDATE", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
