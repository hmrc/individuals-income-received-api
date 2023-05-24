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

package v2.endpoints

import api.models.errors._
import api.stubs.{AuditStub, MtdIdLookupStub, DownstreamStub, AuthStub}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v2.fixtures.RetrievePensionsControllerFixture

class RetrievePensionsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"

    def taxYear: String

    val downstreamResponseBody: JsValue = Json.parse(
      """
        |{
        |   "submittedOn": "2020-07-06T09:37:17Z",
        |   "foreignPension": [
        |      {
        |         "countryCode": "DEU",
        |         "amountBeforeTax": 100.23,
        |         "taxTakenOff": 1.23,
        |         "specialWithholdingTax": 2.23,
        |         "foreignTaxCreditRelief": false,
        |         "taxableAmount": 3.23
        |      },
        |      {
        |         "countryCode": "FRA",
        |         "amountBeforeTax": 200.25,
        |         "taxTakenOff": 1.27,
        |         "specialWithholdingTax": 2.50,
        |         "foreignTaxCreditRelief": true,
        |         "taxableAmount": 3.50
        |      }
        |   ],
        |   "overseasPensionContribution": [
        |      {
        |         "customerReference": "PENSIONINCOME245",
        |         "exemptEmployersPensionContribs": 200.23,
        |         "migrantMemReliefQopsRefNo": "QOPS000000",
        |         "dblTaxationRelief": 4.23,
        |         "dblTaxationCountry": "FRA",
        |         "dblTaxationArticle": "AB3211-1",
        |         "dblTaxationTreaty": "Treaty",
        |         "sf74Reference": "SF74-123456"
        |      },
        |      {
        |         "customerReference": "PENSIONINCOME275",
        |         "exemptEmployersPensionContribs": 270.50,
        |         "migrantMemReliefQopsRefNo": "QOPS000245",
        |         "dblTaxationRelief": 5.50,
        |         "dblTaxationCountry": "NGA",
        |         "dblTaxationArticle": "AB3477-5",
        |         "dblTaxationTreaty": "Treaty",
        |         "sf74Reference": "SF74-1235"
        |      }
        |   ]
        |}
    """.stripMargin
    )

    val mtdResponseBody: JsValue = RetrievePensionsControllerFixture.mtdResponseWithHateoas(nino, taxYear)

    def mtdUri: String = s"/pensions/$nino/$taxYear"

    def downstreamUri: String

    def setupStubs(): Unit = ()

    def request: WSRequest = {
      AuthStub.authorised()
      AuditStub.audit()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  private trait NonTysTest extends Test {

    def taxYear: String       = "2021-22"
    def downstreamUri: String = s"/income-tax/income/pensions/$nino/$taxYear"
  }

  private trait TysIfsTest extends Test {

    def taxYear: String       = "2023-24"
    def downstreamUri: String = s"/income-tax/income/pensions/23-24/$nino"
  }

  "Calling the 'retrieve pensions' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest with Test {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, downstreamResponseBody)

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid TYS request is made" in new TysIfsTest with Test {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, OK, downstreamResponseBody)

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
      "validation error" when {

        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest with Test {

            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2015-17", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2018-19", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest with Test {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamStatus, errorBody(downstreamCode))

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
             |   "reason": "message"
             |}
            """.stripMargin

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
