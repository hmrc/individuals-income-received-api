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
import support.V1IntegrationSpec
import api.models.errors._
import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateAmendNonPayeEmploymentControllerISpec extends V1IntegrationSpec {

  val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "tips": 100.23
      |}
      |""".stripMargin
  )

  val nonsenseBodyJson: JsValue = Json.parse(
    """
      |{
      |  "aField": "aValue"
      |}
       """.stripMargin
  )

  val nonsenseBodyError: MtdError = RuleIncorrectOrEmptyBodyError.copy(
    paths = Some(
      Seq(
        "/tips"
      ))
  )

  val missingFieldsJson: JsValue = Json.parse(
    """
      |{}
     """.stripMargin
  )

  private val invalidTipsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "tips": 100.234
      |}
      |""".stripMargin
  )

  val invalidTipsError: MtdError = ValueFormatError.copy(paths = Some(Seq("/tips")))

  val validNino: String    = "AA123456A"
  val validTaxYear: String = "2020-21"

  private trait Test {

    val nino: String    = validNino
    val taxYear: String = validTaxYear

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/employments/non-paye/$nino/$taxYear",
         |         "method":"PUT",
         |         "rel":"create-and-amend-non-paye-employment-income"
         |      },
         |      {
         |         "href":"/individuals/income-received/employments/non-paye/$nino/$taxYear",
         |         "method":"GET",
         |         "rel":"self"
         |      },
         |      {
         |         "href":"/individuals/income-received/employments/non-paye/$nino/$taxYear",
         |         "method":"DELETE",
         |         "rel":"delete-non-paye-employment-income"
         |      }
         |   ]
         |}
    """.stripMargin
    )

    def uri: String = s"/employments/non-paye/$nino/$taxYear "

    def ifsUri: String = s"/income-tax/income/employments/non-paye/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

  }

  "Calling Create and amend Non-PAYE employment income endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, ifsUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(validRequestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 400 with multiple errors" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedError: MtdError,
                                expectedErrors: Option[ErrorWrapper],
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedError.code} error${scenario.fold("")(scenario => s" for $scenario scenario")}" in new Test {
            override val nino: String    = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe expectedErrors.fold(Json.toJson(expectedError))(errorWrapper => Json.toJson(errorWrapper))
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          // Path errors
          ("AA1123A", validTaxYear, validRequestBodyJson, BAD_REQUEST, NinoFormatError, None, None),
          (validNino, "20177", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError, None, None),
          (validNino, "2015-17", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None, None),
          (validNino, "2018-19", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None, None),
          (validNino, getCurrentTaxYear, validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotEndedError, None, None),

          // Body Errors
          (validNino, validTaxYear, JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None, Some("emptyBody")),
          (validNino, validTaxYear, nonsenseBodyJson, BAD_REQUEST, nonsenseBodyError, None, Some("nonsenseBody")),
          (validNino, validTaxYear, missingFieldsJson, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None, Some("missingFields")),
          (validNino, validTaxYear, invalidTipsRequestBodyJson, BAD_REQUEST, invalidTipsError, None, Some("tipsRule"))
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
              DownstreamStub.onError(DownstreamStub.PUT, ifsUri, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request().put(validRequestBodyJson))
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
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_BEFORE_TAX_YEAR", BAD_REQUEST, RuleTaxYearNotEndedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
