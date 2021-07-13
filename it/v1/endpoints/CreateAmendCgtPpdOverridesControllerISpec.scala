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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateAmendCgtPpdOverridesControllerISpec extends IntegrationBaseSpec {

  val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "submissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.78
      |         },
      |         {
      |            "submissionId": "AB0000000098",
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "submissionId": "AB0000000098",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23,
      |             "amountOfNetGain": 4567.89
      |         },
      |         {
      |             "submissionId": "AB0000000091",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23,
      |             "amountOfNetLoss": 4567.89
      |         }
      |    ]
      |}
      |""".stripMargin
  )

  val nonsenseDataJson: JsValue = Json.parse(
    """
      |{
      |  "aField": "aValue"
      |}
       """.stripMargin
  )

  val emptyFieldsJson: JsValue = Json.parse(
    """
      |{
      |   "multiplePropertyDisposals": [],
      |   "singlePropertyDisposals": []
      |}
     """.stripMargin
  )

  val emptyFieldsError: MtdError = RuleIncorrectOrEmptyBodyError.copy(
    paths = Some(Seq("/multiplePropertyDisposals", "/singlePropertyDisposals"))
  )

  val missingFieldsJson: JsValue = Json.parse(
    """
      |{
      |   "multiplePropertyDisposals": [{}],
      |   "singlePropertyDisposals": [{}]
      |}
     """.stripMargin
  )

  val missingFieldsError: MtdError = RuleIncorrectOrEmptyBodyError.copy(
    paths = Some(Seq(
      "/multiplePropertyDisposals/0/submissionId",

      "/singlePropertyDisposals/0/submissionId",
      "/singlePropertyDisposals/0/completionDate",
      "/singlePropertyDisposals/0/disposalProceeds",
      "/singlePropertyDisposals/0/acquisitionDate",
      "/singlePropertyDisposals/0/acquisitionAmount",
      "/singlePropertyDisposals/0/improvementCosts",
      "/singlePropertyDisposals/0/additionalCosts",
      "/singlePropertyDisposals/0/prfAmount",
      "/singlePropertyDisposals/0/otherReliefAmount"
    ))
  )

  val gainAndLossJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "submissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.78,
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "submissionId": "AB0000000098",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23,
      |             "amountOfNetGain": 4567.89,
      |             "amountOfNetLoss": 50000.99
      |         }
      |    ]
      |}
      |""".stripMargin
  )

  val gainAndLossErrors: ErrorWrapper = ErrorWrapper(
    correlationId = "",
    BadRequestError,
    Some(Seq(
      RuleAmountGainLossError.copy(
        paths = Some(Seq(
          "/multiplePropertyDisposals/0",
          "/singlePropertyDisposals/0"
        ))
      ),
      RuleLossesGreaterThanGainError.copy(
        paths = Some(Seq(
          "/singlePropertyDisposals/0"
        ))
      ),
    ))
  )


  private val invalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "submissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.787385
      |         },
      |         {
      |            "submissionId": "AB0000000092",
      |            "amountOfNetLoss": -134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "submissionId": "AB0000000092",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24999,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45346,
      |             "improvementCosts": 233.4628,
      |             "additionalCosts": 423.34829,
      |             "prfAmount": -2324.67,
      |             "otherReliefAmount": -3434.23,
      |             "lossesFromThisYear": 436.23297423,
      |             "lossesFromPreviousYear": 234.2334728,
      |             "amountOfNetGain": 4567.8974726
      |         },
      |         {
      |             "submissionId": "AB0000000092",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": -454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45837,
      |             "improvementCosts": 233.4628,
      |             "additionalCosts": -423.34,
      |             "prfAmount": 2324.678372,
      |             "otherReliefAmount": -3434.23,
      |             "lossesFromThisYear": 436.23287,
      |             "lossesFromPreviousYear": -234.23,
      |             "amountOfNetLoss": 4567.8983724
      |         }
      |    ]
      |}
      |""".stripMargin
  )

  val invalidValueErrors: ErrorWrapper = ErrorWrapper(
    correlationId = "",
    BadRequestError,
    Some(Seq(
      ValueFormatError.copy(
        message = "The field should be between 0 and 99999999999.99",
        paths = Some(Seq(
          "/multiplePropertyDisposals/0/amountOfNetGain",
          "/multiplePropertyDisposals/1/amountOfNetLoss",
          "/singlePropertyDisposals/0/disposalProceeds",
          "/singlePropertyDisposals/0/acquisitionAmount",
          "/singlePropertyDisposals/0/improvementCosts",
          "/singlePropertyDisposals/0/additionalCosts",
          "/singlePropertyDisposals/0/prfAmount",
          "/singlePropertyDisposals/0/otherReliefAmount",
          "/singlePropertyDisposals/0/lossesFromThisYear",
          "/singlePropertyDisposals/0/lossesFromPreviousYear",
          "/singlePropertyDisposals/0/amountOfNetGain",
          "/singlePropertyDisposals/1/disposalProceeds",
          "/singlePropertyDisposals/1/acquisitionAmount",
          "/singlePropertyDisposals/1/improvementCosts",
          "/singlePropertyDisposals/1/additionalCosts",
          "/singlePropertyDisposals/1/prfAmount",
          "/singlePropertyDisposals/1/otherReliefAmount",
          "/singlePropertyDisposals/1/lossesFromThisYear",
          "/singlePropertyDisposals/1/lossesFromPreviousYear",
          "/singlePropertyDisposals/1/amountOfNetLoss"
        ))
      ))
    )
  )

  val formatPropertyDisposalsJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "submissionId": "notAnID",
      |            "amountOfNetGain": 1234.78
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "submissionId": "notAnID",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23,
      |             "amountOfNetGain": 4567.89
      |         }
      |    ]
      |}
      |""".stripMargin
  )

  val formatPropertyDisposalsErrors: ErrorWrapper = ErrorWrapper(
    correlationId = "",
    BadRequestError,
    Some(Seq(
      DateFormatError.copy(
        paths = Some(Seq(
          "singlePropertyDisposals/0/completionDate",
          "singlePropertyDisposals/0/acquisitionDate"
        ))
      ),
      PPDSubmissionIdFormatError.copy(
        paths = Some(Seq(
          "multiplePropertyDisposals/0/submissionId",
          "singlePropertyDisposals/0/submissionId",
        ))
      )
    ))
  )

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2021-22"
    val correlationId: String = "X-123"

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
         |         "method":"PUT",
         |         "rel":"create-and-amend-report-and-pay-capital-gains-tax-on-property-overrides"
         |      },
         |      {
         |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
         |         "method":"DELETE",
         |         "rel":"delete-report-and-pay-capital-gains-tax-on-property-overrides"
         |      },
         |      {
         |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
         |         "method":"GET",
         |         "rel":"self"
         |      }
         |   ]
         |}
    """.stripMargin
    )

    def uri: String = s"/disposals/residential-property/$nino/$taxYear/ppd"

    def ifsUri: String = s"/income-tax/income/disposals/residential-property/ppd/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling 'Create and Amend 'Report and Pay Capital Gains Tax on Property' Overrides' endpoint" should {
    "return a 204 status code" when {
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
            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request.put(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe expectedErrors.fold(Json.toJson(expectedError))(errorWrapper => Json.toJson(errorWrapper))
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          // Path errors
          ("AA1123A", "2020-21", validRequestBodyJson, BAD_REQUEST, NinoFormatError, None, None),
          ("AA123456A", "20177", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError, None, None),
          ("AA123456A", "2015-17", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None, None),
          ("AA123456A", "2018-19", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None, None),

          // Body Errors
          ("AA123456A", "2020-21", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", nonsenseDataJson, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None, Some("nonsenseBody")),
          ("AA123456A", "2020-21", emptyFieldsJson, BAD_REQUEST, emptyFieldsError, None, Some("emptyFields")),
          ("AA123456A", "2020-21", missingFieldsJson, BAD_REQUEST, missingFieldsError, None, Some("missingFields")),
          ("AA123456A", "2020-21", gainAndLossJson, BAD_REQUEST, BadRequestError, Some(gainAndLossErrors), Some("gainAndLossRule")),
          ("AA123456A", "2020-21", invalidValueRequestBodyJson, BAD_REQUEST, BadRequestError, Some(invalidValueErrors), Some("invalidNumValues")),
          ("AA123456A", "2020-21", formatPropertyDisposalsJson, BAD_REQUEST, BadRequestError, Some(formatPropertyDisposalsErrors), Some("formatPropertyDisposals")),
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
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
          (NOT_FOUND, "PPD_SUBMISSIONID_NOT_FOUND", NOT_FOUND, NotFoundError),
          (NOT_FOUND, "NO_PPD_SUBMISSIONS_FOUND", NOT_FOUND, NotFoundError),
          (CONFLICT, "DUPLICATE_SUBMISSION", INTERNAL_SERVER_ERROR, DownstreamError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_BEFORE_TAX_YEAR", BAD_REQUEST, RuleTaxYearNotEndedError),
          (UNPROCESSABLE_ENTITY, "INVALID_DISPOSAL_TYPE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
