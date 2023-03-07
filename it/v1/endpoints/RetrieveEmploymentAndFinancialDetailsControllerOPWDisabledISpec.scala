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
import v1.fixtures.RetrieveFinancialDetailsControllerFixture._

class RetrieveEmploymentAndFinancialDetailsControllerOPWDisabledISpec extends IntegrationBaseSpec {

  // Disable opw feature switch
  override def servicesConfig: Map[String, Any] = {
    super.servicesConfig ++ Map(
      "feature-switch.opw.enabled" -> "false"
    )
  }

  private trait Test {

    val nino: String           = "AA123456A"
    val employmentId: String   = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    val source: Option[String] = Some("hmrcHeld")

    def taxYear: String

    def mtdUri: String = s"/employments/$nino/$taxYear/$employmentId/financial-details"

    def mtdQueryParams: Seq[(String, String)] =
      Seq("source" -> source)
        .collect { case (k, Some(v)) =>
          (k, v)
        }

    def downstreamTaxYear: String
    def downstreamUri: String

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(mtdUri)
        .addQueryStringParameters(mtdQueryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "downstream message"
         |}
            """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String           = "2019-20"
    def downstreamTaxYear: String = "2019-20"
    def downstreamUri: String     = s"/income-tax/income/employments/$nino/$downstreamTaxYear/$employmentId"
  }

  private trait TysIfsTest extends Test {
    def taxYear: String           = "2023-24"
    def downstreamTaxYear: String = "23-24"
    def downstreamUri: String     = s"/income-tax/income/employments/$downstreamTaxYear/$nino/$employmentId"
  }

  "Calling retrieve employment and financial details endpoint" should {
    "return 200 status code" when {
      "a valid request with all parameters is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, Map("view" -> "HMRC-HELD"), OK, downstreamJson)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe mtdResponseWithoutOPWWithHateoas(nino, taxYear, employmentId)
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a valid request with all parameters and a Tax Year Specific (TYS) tax year is made" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, Map("view" -> "HMRC-HELD"), OK, downstreamJson)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe mtdResponseWithoutOPWWithHateoas(nino, taxYear, employmentId)
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a valid request without employment source is made" in new NonTysTest {

        override val source: Option[String] = None

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, Map("view" -> "LATEST"), OK, downstreamJson)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe mtdResponseWithoutOPWWithHateoas(nino, taxYear, employmentId)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestEmploymentId: String,
                                empSource: Option[String],
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new NonTysTest {

            override val nino: String           = requestNino
            override val taxYear: String        = requestTaxYear
            override val employmentId: String   = requestEmploymentId
            override val source: Option[String] = empSource

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
          ("AA1123A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", None, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20122", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", None, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2019-20", "ABCDE12345FG", None, BAD_REQUEST, EmploymentIdFormatError),
          ("AA123456A", "2019-21", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", None, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2016-17", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", None, BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Some("SOURCE"), BAD_REQUEST, SourceFormatError)
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
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, Map("view" -> "HMRC-HELD"), downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request.get)
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_EMPLOYMENT_ID", BAD_REQUEST, EmploymentIdFormatError),
          (BAD_REQUEST, "INVALID_VIEW", BAD_REQUEST, SourceFormatError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
