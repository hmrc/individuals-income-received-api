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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class AddCustomEmploymentControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"
    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "employerRef": "123/AZ12334",
        |  "employerName": "AMD infotech Ltd",
        |  "startDate": "2019-01-01",
        |  "cessationDate": "2020-06-01",
        |  "payrollId": "124214112412",
        |  "occupationalPension": false
        |}
      """.stripMargin
    )

    val responseJson: JsValue = Json.parse(
      s"""
         |{
         |   "employmentId": "$employmentId"
         |}
        """.stripMargin
    )

    val wrappedResponseJson: JsValue = Json.parse(
      s"""
         |{
         |   "employmentId": "$employmentId",
         |   "links":[
         |      {
         |         "href": "/individuals/income-received/employments/$nino/$taxYear",
         |         "rel": "list-employments",
         |         "method": "GET"
         |      },
         |      {
         |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
         |         "rel": "self",
         |         "method": "GET"
         |      },
         |      {
         |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
         |         "rel": "amend-custom-employment",
         |         "method": "PUT"
         |      },
         |      {
         |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
         |         "rel": "delete-custom-employment",
         |         "method": "DELETE"
         |      }
         |   ]
         |}
        """.stripMargin
    )

    def uri: String = s"/employments/$nino/$taxYear"

    def ifsUri: String = s"/income-tax/income/employments/$nino/$taxYear/custom"

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

  "Calling the 'add custom employment' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, ifsUri, OK, responseJson)
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe wrappedResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      val validRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2019-01-01",
          |  "cessationDate": "2020-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val emptyRequestJson: JsValue = JsObject.empty

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": false,
          |  "employerName": false,
          |  "startDate": false,
          |  "cessationDate": false,
          |  "payrollId": false,
          |  "occupationalPension": 13
          |}
        """.stripMargin
      )

      val missingFieldRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334"
          |}
        """.stripMargin
      )

      val invalidEmployerRefRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "notValid",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2019-01-01",
          |  "cessationDate": "2020-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val invalidEmployerNameRequestJson: JsValue = Json.parse(
        s"""
           |{
           |  "employerRef": "123/AZ12334",
           |  "employerName": "${"a" * 100}",
           |  "startDate": "2019-01-01",
           |  "cessationDate": "2020-06-01",
           |  "payrollId": "124214112412",
           |  "occupationalPension": false
           |}
      """.stripMargin
      )

      val invalidStartDateRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "notValid",
          |  "cessationDate": "2020-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val invalidCessationDateRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2019-01-01",
          |  "cessationDate": "notValid",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val invalidPayrollIdRequestJson: JsValue = Json.parse(
        s"""
           |{
           |  "employerRef": "123/AZ12334",
           |  "employerName": "AMD infotech Ltd",
           |  "startDate": "2019-01-01",
           |  "cessationDate": "2020-06-01",
           |  "payrollId": "${"a" * 100}",
           |  "occupationalPension": false
           |}
      """.stripMargin
      )

      val invalidDateOrderRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2020-01-01",
          |  "cessationDate": "2019-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val startDateLateRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2023-01-01",
          |  "cessationDate": "2023-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val cessationDateEarlyRequestJson: JsValue = Json.parse(
        """
          |{
          |  "employerRef": "123/AZ12334",
          |  "employerName": "AMD infotech Ltd",
          |  "startDate": "2018-01-01",
          |  "cessationDate": "2018-06-01",
          |  "payrollId": "124214112412",
          |  "occupationalPension": false
          |}
      """.stripMargin
      )

      val invalidFieldType: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(
          List(
            "/employerRef",
            "/employerName",
            "/payrollId",
            "/cessationDate",
            "/startDate",
            "/occupationalPension"
          ))
      )

      val missingMandatoryFieldErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(
          List(
            "/employerName",
            "/startDate",
            "/occupationalPension"
          ))
      )

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
          ("AA123456A", "20177", validRequestJson, BAD_REQUEST, TaxYearFormatError, None),
          ("AA123456A", "2015-17", validRequestJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          ("AA123456A", "2015-16", validRequestJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          ("AA123456A", getCurrentTaxYear, validRequestJson, BAD_REQUEST, RuleTaxYearNotEndedError, None),
          ("AA123456A", "2019-20", invalidEmployerRefRequestJson, BAD_REQUEST, EmployerRefFormatError, None),
          ("AA123456A", "2019-20", invalidEmployerNameRequestJson, BAD_REQUEST, EmployerNameFormatError, None),
          ("AA123456A", "2019-20", invalidPayrollIdRequestJson, BAD_REQUEST, PayrollIdFormatError, None),
          ("AA123456A", "2019-20", invalidStartDateRequestJson, BAD_REQUEST, StartDateFormatError, None),
          ("AA123456A", "2019-20", invalidCessationDateRequestJson, BAD_REQUEST, CessationDateFormatError, None),
          ("AA123456A", "2019-20", invalidDateOrderRequestJson, BAD_REQUEST, RuleCessationDateBeforeStartDateError, None),
          ("AA123456A", "2019-20", startDateLateRequestJson, BAD_REQUEST, RuleStartDateAfterTaxYearEndError, None),
          ("AA123456A", "2019-20", cessationDateEarlyRequestJson, BAD_REQUEST, RuleCessationDateBeforeTaxYearStartError, None),
          ("AA123456A", "2019-20", emptyRequestJson, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          ("AA123456A", "2019-20", nonValidRequestBodyJson, BAD_REQUEST, invalidFieldType, Some("(wrong field type)")),
          ("AA123456A", "2019-20", missingFieldRequestBodyJson, BAD_REQUEST, missingMandatoryFieldErrors, Some("(missing mandatory fields)"))
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
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (UNPROCESSABLE_ENTITY, "NOT_SUPPORTED_TAX_YEAR", BAD_REQUEST, RuleTaxYearNotEndedError),
          (UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", BAD_REQUEST, RuleStartDateAfterTaxYearEndError),
          (UNPROCESSABLE_ENTITY, "INVALID_CESSATION_DATE", BAD_REQUEST, RuleCessationDateBeforeTaxYearStartError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}