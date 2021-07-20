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

class CreateAmendCgtResidentialPropertyDisposalsControllerISpec extends IntegrationBaseSpec {

  val validRequestJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2021-01-29",
      |         "completionDate": "2021-03-25",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
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
    paths = Some(Seq(
      "/disposals/0/disposalDate",
      "/disposals/0/completionDate",
      "/disposals/0/disposalProceeds",
      "/disposals/0/acquisitionDate",
      "/disposals/0/acquisitionAmount"
    ))
  )

  val decimalsTooBigJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2021-01-29",
      |         "completionDate": "2021-03-25",
      |         "disposalProceeds": 100000000000.00,
      |         "acquisitionDate": "2021-03-22",
      |         "acquisitionAmount": 100000000000.00,
      |         "improvementCosts": 100000000000.00,
      |         "additionalCosts": 100000000000.00,
      |         "prfAmount": 100000000000.00,
      |         "otherReliefAmount": 100000000000.00,
      |         "lossesFromThisYear": 100000000000.00,
      |         "lossesFromPreviousYear": 100000000000.00,
      |         "amountOfNetGain": 100000000000.00,
      |         "amountOfNetLoss": 100000000000.00,
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val decimalsTooSmallJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2021-01-29",
      |         "completionDate": "2021-03-25",
      |         "disposalProceeds": -0.1,
      |         "acquisitionDate": "2021-03-22",
      |         "acquisitionAmount": -0.1,
      |         "improvementCosts": -0.1,
      |         "additionalCosts": -0.1,
      |         "prfAmount": -0.1,
      |         "otherReliefAmount": -0.1,
      |         "lossesFromThisYear": -0.1,
      |         "lossesFromPreviousYear": -0.1,
      |         "amountOfNetGain": -0.1,
      |         "amountOfNetLoss": -0.1,
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val DecimalsOutOfRangeError: MtdError = ValueFormatError.copy(
    message = "The value must be between 0 and 99999999999.99",
    paths = Some(Seq(
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
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "202105-07",
      |         "completionDate": "21-05-07",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-5-7",
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
    message = "The value must be in the format YYYY-MM-DD",
    paths = Some(Seq(
      "/disposals/0/disposalDate",
      "/disposals/0/completionDate",
      "/disposals/0/acquisitionDate"
    ))
  )

  val customerRefTooLongJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "ThisIs91CharactersLongCGTDIS000000000000000000000000000000000000000000000000000000000000001",
      |         "disposalDate": "2021-01-29",
      |         "completionDate": "2021-03-25",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
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
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "",
      |         "disposalDate": "2021-01-29",
      |         "completionDate": "2021-03-25",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
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
    paths = Some(Seq(
      "/disposals/0/customerReference"
    ))
  )

  val completionBeforeDisposalJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2021-01-29",
      |         "completionDate": "2021-01-25",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
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

  val completionBeforeDisposalError: MtdError = RuleCompletionDateBeforeDisposalDateError.copy(
    message = "The completionDate must not be earlier than the disposalDate",
    paths = Some(Seq(
      "/disposals/0"
    ))
  )

  val acquisitionBeforeDisposalJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2021-01-29",
      |         "completionDate": "2021-03-25",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-01-22",
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

  val acquisitionBeforeDisposalError: MtdError = RuleAcquisitionDateBeforeDisposalDateError.copy(
    message = "The acquisitionDate must not be earlier than the disposalDate",
    paths = Some(Seq(
      "/disposals/0"
    ))
  )

  val completionDateJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2021-01-29",
      |         "completionDate": "2021-03-01",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
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

  val completionDateError: MtdError = RuleCompletionDateError.copy(
    message = "The completionDate must be on or after 7th March of the specified tax year and not a date in the future",
    paths = Some(Seq(
      "/disposals/0/completionDate"
    ))
  )

  val disposalDateJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2020-01-29",
      |         "completionDate": "2021-03-25",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetLoss": 1999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val disposalDateError: MtdError = RuleDisposalDateError.copy(
    message = "The disposalDate must be within the specified tax year",
    paths = Some(Seq(
      "/disposals/0/disposalDate"
    ))
  )

  val gainLossJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2020-01-29",
      |         "completionDate": "2021-03-25",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetGain": 1999.99,
      |         "amountOfNetLoss": 1999.99,
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val gainLossError: MtdError = RuleGainLossError.copy(
    message = "Only one of gain or loss values can be provided",
    paths = Some(Seq(
      "/disposals/0"
    ))
  )

  val lossGreaterThanGainJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2020-01-29",
      |         "completionDate": "2021-03-25",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetGain": 1999.99,
      |         "amountOfNetLoss": 1999.99,
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val lossGreaterThanGainError: MtdError = RuleLossesGreaterThanGainError.copy(
    message = "The lossesFromThisYear or lossesFromPreviousYear is greater than the gain",
    paths = Some(Seq(
      "/disposals/0"
    ))
  )

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2021-22"

    val mtdResponse: JsValue = Json.parse(
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
         |         "method":"GET",
         |         "rel":"self"
         |      },
         |      {
         |         "href":"/individuals/income-received/disposals/other-gains/$nino/$taxYear",
         |         "method":"DELETE",
         |         "rel":"delete-other-capital-gains-and-disposals"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    def uri: String = s"/disposals/residential-property/$nino/$taxYear"

    def ifsUri: String = s"/income-tax/income/disposals/residential-property/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'create and amend other CGT' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, ifsUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request.put(validRequestJson))
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
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
          ("AA1123A", "2019-20", validRequestJson, BAD_REQUEST, NinoFormatError, None, None),
          ("AA123456A", "20177", validRequestJson, BAD_REQUEST, TaxYearFormatError, None, None),
          ("AA123456A", "2015-17", validRequestJson, BAD_REQUEST, RuleTaxYearRangeInvalidError, None, None),
          ("AA123456A", "2018-19", validRequestJson, BAD_REQUEST, RuleTaxYearNotSupportedError, None, None),

          // Body errors
          ("AA123456A", "2020-21", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", emptyDisposalsJson, BAD_REQUEST, emptyDisposalsError, None, Some("empty disposals")),
          ("AA123456A", "2020-21", missingFieldsJson, BAD_REQUEST, missingFieldsError, None, Some("missing all mandatory fields")),
          ("AA123456A", "2020-21", decimalsTooBigJson, BAD_REQUEST, DecimalsOutOfRangeError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", decimalsTooSmallJson, BAD_REQUEST, DecimalsOutOfRangeError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", datesNotFormattedJson, BAD_REQUEST, datesNotFormattedError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", customerRefTooLongJson, BAD_REQUEST, customerRefError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", customerRefTooShortJson, BAD_REQUEST, customerRefError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", completionBeforeDisposalJson, BAD_REQUEST, completionBeforeDisposalError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", acquisitionBeforeDisposalJson, BAD_REQUEST, acquisitionBeforeDisposalError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", completionDateJson, BAD_REQUEST, completionDateError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", disposalDateJson, BAD_REQUEST, disposalDateError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", gainLossJson, BAD_REQUEST, gainLossError, None, Some("emptyBody")),
          ("AA123456A", "2020-21", lossGreaterThanGainJson, BAD_REQUEST, lossGreaterThanGainError, None, Some("emptyBody"))
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
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
          (UNPROCESSABLE_ENTITY, "INVALID_DISPOSAL_DATE", INTERNAL_SERVER_ERROR, DownstreamError),
          (UNPROCESSABLE_ENTITY, "INVALID_COMPLETION_DATE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}