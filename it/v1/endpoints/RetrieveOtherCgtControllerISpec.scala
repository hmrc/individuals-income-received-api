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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.V1R6IntegrationSpec
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import v1r6.stubs.AuthStub

class RetrieveOtherCgtControllerISpec extends V1R6IntegrationSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"

    val ifsResponse: JsValue = Json.parse(
      """
        |{
        |   "submittedOn":"2021-05-07T16:18:44.403Z",
        |   "disposals":[
        |      {
        |         "assetType":"otherProperty",
        |         "assetDescription":"string",
        |         "acquisitionDate":"2021-05-07",
        |         "disposalDate":"2021-05-07",
        |         "disposalProceeds":59999999999.99,
        |         "allowableCosts":59999999999.99,
        |         "gain":59999999999.99,
        |         "claimOrElectionCodes":[
        |            "OTH"
        |         ],
        |         "gainAfterRelief":59999999999.99,
        |         "rttTaxPaid":59999999999.99
        |      }
        |   ],
        |   "nonStandardGains":{
        |      "carriedInterestGain":19999999999.99,
        |      "carriedInterestRttTaxPaid":19999999999.99,
        |      "attributedGains":19999999999.99,
        |      "attributedGainsRttTaxPaid":19999999999.99,
        |      "otherGains":19999999999.99,
        |      "otherGainsRttTaxPaid":19999999999.99
        |   },
        |   "losses":{
        |      "broughtForwardLossesUsedInCurrentYear":29999999999.99,
        |      "setAgainstInYearGains":29999999999.99,
        |      "setAgainstInYearGeneralIncome":29999999999.99,
        |      "setAgainstEarlierYear":29999999999.99
        |   },
        |   "adjustments":-39999999999.99
        |}
     """.stripMargin
    )

    val mtdResponse: JsValue = ifsResponse.as[JsObject] ++ Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
         |         "method":"PUT",
         |         "rel":"create-and-amend-other-capital-gains-and-disposals"
         |      },
         |      {
         |         "href":"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
         |         "method":"DELETE",
         |         "rel":"delete-other-capital-gains-and-disposals"
         |      },
         |      {
         |         "href":"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
         |         "method":"GET",
         |         "rel":"self"
         |      }
         |   ]
         |}
       """.stripMargin
    ).as[JsObject]

    def uri: String = s"/disposals/other-gains/$nino/$taxYear"

    def ifsUri: String = s"/income-tax/income/disposals/other-gains/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'retrieve other CGT' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, ifsUri, OK, ifsResponse)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear

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
          ("AA1123A", "2019-20", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2015-17", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2018-19", BAD_REQUEST, RuleTaxYearNotSupportedError))

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, ifsUri, ifsStatus, errorBody(ifsCode))
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
             |   "reason": "ifs message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}