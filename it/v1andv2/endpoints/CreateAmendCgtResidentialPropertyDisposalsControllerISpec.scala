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
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.{IntegrationBaseSpec, WireMockMethods}

class CreateAmendCgtResidentialPropertyDisposalsControllerISpec extends IntegrationBaseSpec with WireMockMethods {

  val validDisposalDate: String    = "2020-03-27"
  val validCompletionDate: String  = "2020-03-29"
  val validAcquisitionDate: String = "2020-03-25"

  val validRequestJson: JsValue = Json.parse(
    s"""
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "$validDisposalDate",
      |         "completionDate": "$validCompletionDate",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "$validAcquisitionDate",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetGain": 1999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val noMeaningfulDataJson: JsValue = Json.parse(
    """
      |{
      |  "aField": "aValue"
      |}
       """.stripMargin
  )

  val emptyDisposalsJson: JsValue = Json.parse(
    """
      |{
      |   "disposals": []
      |}
     """.stripMargin
  )

  val emptyDisposalsError: MtdError = RuleIncorrectOrEmptyBodyError.copy(
    paths = Some(Seq("/disposals"))
  )

  val missingFieldsJson: JsValue = Json.parse(
    """
      |{
      |   "disposals": [{}]
      |}
     """.stripMargin
  )

  val missingFieldsError: MtdError = RuleIncorrectOrEmptyBodyError.copy(
    paths = Some(
      Seq(
        "/disposals/0/acquisitionAmount",
        "/disposals/0/acquisitionDate",
        "/disposals/0/completionDate",
        "/disposals/0/disposalDate",
        "/disposals/0/disposalProceeds"
      ))
  )

  val decimalsTooBigJson: JsValue = Json.parse(
    s"""
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "$validDisposalDate",
      |         "completionDate": "$validCompletionDate",
      |         "disposalProceeds": 100000000000.00,
      |         "acquisitionDate": "$validAcquisitionDate",
      |         "acquisitionAmount": 100000000000.00,
      |         "improvementCosts": 100000000000.00,
      |         "additionalCosts": 100000000000.00,
      |         "prfAmount": 100000000000.00,
      |         "otherReliefAmount": 100000000000.00,
      |         "lossesFromThisYear": 100000000000.00,
      |         "lossesFromPreviousYear": 100000000000.00,
      |         "amountOfNetGain": 100000000000.00,
      |         "amountOfNetLoss": 100000000000.00
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val decimalsTooSmallJson: JsValue = Json.parse(
    s"""
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "$validDisposalDate",
      |         "completionDate": "$validCompletionDate",
      |         "disposalProceeds": -0.1,
      |         "acquisitionDate": "$validAcquisitionDate",
      |         "acquisitionAmount": -0.1,
      |         "improvementCosts": -0.1,
      |         "additionalCosts": -0.1,
      |         "prfAmount": -0.1,
      |         "otherReliefAmount": -0.1,
      |         "lossesFromThisYear": -0.1,
      |         "lossesFromPreviousYear": -0.1,
      |         "amountOfNetGain": -0.1,
      |         "amountOfNetLoss": -0.1
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val DecimalsOutOfRangeError: MtdError = ValueFormatError.copy(
    message = "The value must be between 0 and 99999999999.99",
    paths = Some(
      Seq(
        "/disposals/0/disposalProceeds",
        "/disposals/0/acquisitionAmount",
        "/disposals/0/improvementCosts",
        "/disposals/0/additionalCosts",
        "/disposals/0/prfAmount",
        "/disposals/0/otherReliefAmount",
        "/disposals/0/lossesFromThisYear",
        "/disposals/0/lossesFromPreviousYear",
        "/disposals/0/amountOfNetGain",
        "/disposals/0/amountOfNetLoss"
      ))
  )

  val datesNotFormattedJson: JsValue = Json.parse(
    s"""
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "202005-07",
      |         "completionDate": "21-05-07",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2020-5-7",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetGain": 1999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val datesNotFormattedError: MtdError = DateFormatError.copy(
    message = "The field should be in the format YYYY-MM-DD",
    paths = Some(
      Seq(
        "/disposals/0/disposalDate",
        "/disposals/0/completionDate",
        "/disposals/0/acquisitionDate"
      ))
  )

  val customerRefTooLongJson: JsValue = Json.parse(
    s"""
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "ThisIs91CharactersLongCGTDIS000000000000000000000000000000000000000000000000000000000000001",
      |         "disposalDate": "$validDisposalDate",
      |         "completionDate": "$validCompletionDate",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "$validAcquisitionDate",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetGain": 1999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val customerRefTooShortJson: JsValue = Json.parse(
    s"""
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "",
      |         "disposalDate": "$validDisposalDate",
      |         "completionDate": "$validCompletionDate",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "$validAcquisitionDate",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetGain": 1999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val customerRefError: MtdError = CustomerRefFormatError.copy(
    message = "The provided customer reference is invalid",
    paths = Some(
      Seq(
        "/disposals/0"
      ))
  )

  val gainLossJson: JsValue = Json.parse(
    s"""
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "$validDisposalDate",
      |         "completionDate": "$validCompletionDate",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "$validAcquisitionDate",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetGain": 19999.99,
      |         "amountOfNetLoss": 19999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val gainLossError: MtdError = RuleGainLossError.copy(
    message = "Only one of gain or loss values can be provided",
    paths = Some(
      Seq(
        "/disposals/0"
      ))
  )

  trait Test {

    val nino: String = "AA123456A"
    def taxYear: String

    val mtdResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
         |         "method":"PUT",
         |         "rel":"create-and-amend-cgt-residential-property-disposals"
         |      },
         |      {
         |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
         |         "method":"GET",
         |         "rel":"self"
         |      },
         |      {
         |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
         |         "method":"DELETE",
         |         "rel":"delete-cgt-residential-property-disposals"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    def downstreamUri: String

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(s"/disposals/residential-property/$nino/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def verifyNrs(payload: JsValue): Unit =
      verify(
        postRequestedFor(urlEqualTo(s"/mtd-api-nrs-proxy/$nino/itsa-cgt-disposal"))
          .withRequestBody(equalToJson(payload.toString())))

  }

  trait NonTysTest extends Test {
    def taxYear: String = "2019-20"

    def downstreamUri: String = s"/income-tax/income/disposals/residential-property/$nino/2019-20"
  }

  trait TysTest extends Test {
    def taxYear: String = "2023-24"

    override def request: WSRequest =
      super.request.addHttpHeaders("suspend-temporal-validations" -> "true")

    def downstreamUri: String = s"/income-tax/income/disposals/residential-property/23-24/$nino"
  }

  "Calling the 'create and amend other CGT' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request.put(validRequestJson))
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")

        verifyNrs(validRequestJson)
      }

      "any valid request is made for a TYS tax year" in new TysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request.put(validRequestJson))
        response.json shouldBe mtdResponse
        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")

        verifyNrs(validRequestJson)
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedError: MtdError,
                                expectedErrors: Option[ErrorWrapper],
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedError.code} error${scenario.fold("")(scenario => s" for $scenario scenario")}" in new NonTysTest {

            override val nino: String    = requestNino
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
          ("AA1123A", "2019-20", validRequestJson, BAD_REQUEST, NinoFormatError, None, None),
          ("AA123456A", "20177", validRequestJson, BAD_REQUEST, TaxYearFormatError, None, None),
          ("AA123456A", "2015-17", validRequestJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None, None),
          ("AA123456A", "2018-19", validRequestJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None, None),

          // Body errors
          ("AA123456A", "2019-20", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None, Some("emptyBody")),
          ("AA123456A", "2019-20", emptyDisposalsJson, BAD_REQUEST, emptyDisposalsError, None, Some("empty disposals")),
          ("AA123456A", "2019-20", missingFieldsJson, BAD_REQUEST, missingFieldsError, None, Some("missing all mandatory fields")),
          ("AA123456A", "2019-20", decimalsTooBigJson, BAD_REQUEST, DecimalsOutOfRangeError, None, Some("Decimals too big")),
          ("AA123456A", "2019-20", decimalsTooSmallJson, BAD_REQUEST, DecimalsOutOfRangeError, None, Some("Decimals too small")),
          ("AA123456A", "2019-20", datesNotFormattedJson, BAD_REQUEST, datesNotFormattedError, None, Some("incorrect date formats")),
          ("AA123456A", "2019-20", customerRefTooLongJson, BAD_REQUEST, customerRefError, None, Some("bad customer reference")),
          ("AA123456A", "2019-20", customerRefTooShortJson, BAD_REQUEST, customerRefError, None, Some("empty customer reference string")),
          ("AA123456A", "2019-20", gainLossJson, BAD_REQUEST, gainLossError, None, Some("gain and loss provided"))
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request.put(validRequestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")

            verifyNrs(validRequestJson)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "ifs message"
             |}
            """.stripMargin

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "INVALID_DISPOSAL_DATE", BAD_REQUEST, RuleDisposalDateError),
          (UNPROCESSABLE_ENTITY, "INVALID_COMPLETION_DATE", BAD_REQUEST, RuleCompletionDateError),
          (UNPROCESSABLE_ENTITY, "INVALID_ACQUISITION_DATE", BAD_REQUEST, RuleAcquisitionDateAfterDisposalDateError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
