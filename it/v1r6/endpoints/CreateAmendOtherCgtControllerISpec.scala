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

package v1r6.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1r6.controllers.requestParsers.validators.validations.DisposalDateErrorMessages
import v1r6.models.errors._
import v1r6.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class CreateAmendOtherCgtControllerISpec extends IntegrationBaseSpec with DisposalDateErrorMessages {

  val validRequestJson: JsValue = Json.parse(
    """
      |{
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

  val noMeaningfulDataJson: JsValue = Json.parse(
    """
      |{
      |  "aField": "aValue"
      |}
       """.stripMargin
  )

  val emptyFieldsJson: JsValue = Json.parse(
    """
      |{
      |   "disposals": [],
      |   "nonStandardGains": {},
      |   "losses": {}
      |}
     """.stripMargin
  )

  val emptyFieldsError: MtdError = RuleIncorrectOrEmptyBodyError.copy(
    paths = Some(Seq("/disposals", "/nonStandardGains", "/losses"))
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
      "/disposals/0/acquisitionDate",
      "/disposals/0/assetDescription",
      "/disposals/0/disposalProceeds",
      "/disposals/0/assetType",
      "/disposals/0/allowableCosts"
    ))
  )

  val gainAndLossJson: JsValue = Json.parse(
    """
      |{
      |   "disposals": [
      |     {
      |       "assetType":"otherProperty",
      |       "assetDescription":"string",
      |       "acquisitionDate":"2021-05-07",
      |       "disposalDate":"2021-05-07",
      |       "disposalProceeds":59999999999.99,
      |       "allowableCosts":59999999999.99,
      |       "gain":59999999999.99,
      |       "loss":1234123.44,
      |       "claimOrElectionCodes":[
      |          "OTH"
      |       ],
      |       "gainAfterRelief":59999999999.99,
      |       "lossAfterRelief":59999999999.99,
      |       "rttTaxPaid":59999999999.99
      |     }
      |   ]
      |}
     """.stripMargin
  )

  val gainAndLossErrors: ErrorWrapper = ErrorWrapper(
    correlationId = "",
    BadRequestError,
    Some(Seq(
      RuleGainLossError.copy(
        paths = Some(Seq(
          "/disposals/0"
        ))
      ),
      RuleGainAfterReliefLossAfterReliefError.copy(
        paths = Some(Seq(
          "/disposals/0"
        ))
      ))
    )
  )

  val decimalsTooBigJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "assetType":"otherProperty",
      |         "assetDescription":"string",
      |         "acquisitionDate":"2021-05-07",
      |         "disposalDate":"2021-05-07",
      |         "disposalProceeds": 999999999999.99,
      |         "allowableCosts": 999999999999.99,
      |         "gain": 999999999999.99,
      |         "claimOrElectionCodes":[
      |            "OTH"
      |         ],
      |         "gainAfterRelief": 999999999999.99,
      |         "rttTaxPaid": 999999999999.99
      |      }
      |   ],
      |   "nonStandardGains":{
      |      "carriedInterestGain": 999999999999.99,
      |      "carriedInterestRttTaxPaid": 999999999999.99,
      |      "attributedGains": 999999999999.99,
      |      "attributedGainsRttTaxPaid": 999999999999.99,
      |      "otherGains": 999999999999.99,
      |      "otherGainsRttTaxPaid": 999999999999.99
      |   },
      |   "losses":{
      |      "broughtForwardLossesUsedInCurrentYear": 999999999999.99,
      |      "setAgainstInYearGains": 999999999999.99,
      |      "setAgainstInYearGeneralIncome": 999999999999.99,
      |      "setAgainstEarlierYear": 999999999999.99
      |   },
      |   "adjustments": 999999999999.99
      |}
     """.stripMargin
  )

  val decimalsTooSmallJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "assetType":"otherProperty",
      |         "assetDescription":"string",
      |         "acquisitionDate":"2021-05-07",
      |         "disposalDate":"2021-05-07",
      |         "disposalProceeds": -0.01,
      |         "allowableCosts": -0.01,
      |         "gain": -0.01,
      |         "claimOrElectionCodes":[
      |            "OTH"
      |         ],
      |         "gainAfterRelief": -0.01,
      |         "rttTaxPaid": -0.01
      |      }
      |   ],
      |   "nonStandardGains":{
      |      "carriedInterestGain": -0.01,
      |      "carriedInterestRttTaxPaid": -0.01,
      |      "attributedGains": -0.01,
      |      "attributedGainsRttTaxPaid": -0.01,
      |      "otherGains": -0.01,
      |      "otherGainsRttTaxPaid": -0.01
      |   },
      |   "losses":{
      |      "broughtForwardLossesUsedInCurrentYear": -0.01,
      |      "setAgainstInYearGains": -0.01,
      |      "setAgainstInYearGeneralIncome": -0.01,
      |      "setAgainstEarlierYear": -0.01
      |   },
      |   "adjustments": -999999999999.99
      |}
     """.stripMargin
  )

  val positiveDecimalsOutOfRangeError: MtdError = ValueFormatError.copy(
    message = "The value must be between 0 and 99999999999.99",
    paths = Some(Seq(
      "/disposals/0/disposalProceeds",
      "/disposals/0/allowableCosts",
      "/disposals/0/gain",
      "/disposals/0/gainAfterRelief",
      "/disposals/0/rttTaxPaid",
      "/nonStandardGains/carriedInterestGain",
      "/nonStandardGains/carriedInterestRttTaxPaid",
      "/nonStandardGains/attributedGains",
      "/nonStandardGains/attributedGainsRttTaxPaid",
      "/nonStandardGains/otherGains",
      "/nonStandardGains/otherGainsRttTaxPaid",
      "/losses/broughtForwardLossesUsedInCurrentYear",
      "/losses/setAgainstInYearGains",
      "/losses/setAgainstInYearGeneralIncome",
      "/losses/setAgainstEarlierYear"
    ))
  )

  val decimalsOutOfRangeErrors: ErrorWrapper = ErrorWrapper(
    correlationId = "",
    BadRequestError,
    Some(Seq(
      positiveDecimalsOutOfRangeError,
      ValueFormatError.copy(
        message = "The value must be between -99999999999.99 and 99999999999.99",
        paths = Some(Seq(
          "/adjustments"
        ))
      )
    ))
  )

  val formatDisposalsJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "assetType":"notEnumValue",
      |         "assetDescription":"",
      |         "acquisitionDate":"",
      |         "disposalDate":"",
      |         "disposalProceeds":59999999999.99,
      |         "allowableCosts":59999999999.99,
      |         "gain":59999999999.99,
      |         "claimOrElectionCodes":[
      |            "bip",
      |            "bop"
      |         ],
      |         "gainAfterRelief":59999999999.99,
      |         "rttTaxPaid":59999999999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val formatDisposalsErrors: ErrorWrapper = ErrorWrapper(
    correlationId = "",
    BadRequestError,
    Some(Seq(
      DateFormatError.copy(
        paths = Some(Seq(
          "/disposals/0/acquisitionDate",
          "/disposals/0/disposalDate"
        ))
      ),
      AssetDescriptionFormatError.copy(
        paths = Some(Seq(
          "/disposals/0/assetDescription"
        ))
      ),
      AssetTypeFormatError.copy(
        paths = Some(Seq(
          "/disposals/0/assetType"
        ))
      ),
      ClaimOrElectionCodesFormatError.copy(
        paths = Some(Seq(
          "/disposals/0/claimOrElectionCodes/0",
          "/disposals/0/claimOrElectionCodes/1"
        ))
      )
    ))
  )

  val ruleDateJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "assetType":"otherProperty",
      |         "assetDescription":"string",
      |         "acquisitionDate":"2023-05-07",
      |         "disposalDate":"2022-05-07",
      |         "disposalProceeds":59999999999.99,
      |         "allowableCosts":59999999999.99,
      |         "gain":59999999999.99,
      |         "claimOrElectionCodes":[
      |            "OTH"
      |         ],
      |         "gainAfterRelief":59999999999.99,
      |         "rttTaxPaid":59999999999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val ruleDateErrors: ErrorWrapper = ErrorWrapper(
    correlationId = "",
    BadRequestError,
    Some(Seq(
      RuleDisposalDateError.copy(
        paths = Some(Seq(
          "/disposals/0"
        )),
        message = IN_YEAR_NO_LATER_THAN_TODAY
      ),
      RuleAcquisitionDateError.copy(
        paths = Some(Seq(
          "/disposals/0"
        ))
      )
    ))
  )

  val formatNonStandardGainsJson: JsValue = Json.parse(
    """
      |{
      |   "nonStandardGains":{
      |      "carriedInterestRttTaxPaid":19999999999.99,
      |      "attributedGainsRttTaxPaid":19999999999.99,
      |      "otherGainsRttTaxPaid":19999999999.99
      |   }
      |}
     """.stripMargin
  )

  val formatNonStandardGainsError: MtdError = RuleIncorrectOrEmptyBodyError.copy(
    paths = Some(Seq("/nonStandardGains"))
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

    def uri: String = s"/disposals/other-gains/$nino/$taxYear"

    def ifsUri: String = s"/income-tax/income/disposals/other-gains/$nino/$taxYear"

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
          ("AA123456A", "2021-22", JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None, Some("emptyBody")),
          ("AA123456A", "2021-22", noMeaningfulDataJson, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None, Some("nonsenseBody")),
          ("AA123456A", "2021-22", emptyFieldsJson, BAD_REQUEST, emptyFieldsError, None, Some("emptyFields")),
          ("AA123456A", "2021-22", missingFieldsJson, BAD_REQUEST, missingFieldsError, None, Some("missingFields")),
          ("AA123456A", "2021-22", gainAndLossJson, BAD_REQUEST, BadRequestError, Some(gainAndLossErrors), Some("gainAndLossRule")),
          ("AA123456A", "2021-22", decimalsTooBigJson, BAD_REQUEST, BadRequestError, Some(decimalsOutOfRangeErrors), Some("decimalsTooBig")),
          ("AA123456A", "2021-22", decimalsTooSmallJson, BAD_REQUEST, BadRequestError, Some(decimalsOutOfRangeErrors), Some("decimalsTooSmall")),
          ("AA123456A", "2021-22", formatDisposalsJson, BAD_REQUEST, BadRequestError, Some(formatDisposalsErrors), Some("formatDisposals")),
          ("AA123456A", "2021-22", ruleDateJson, BAD_REQUEST, BadRequestError, Some(ruleDateErrors), Some("ruleDate")),
          ("AA123456A", "2021-22", formatNonStandardGainsJson, BAD_REQUEST, formatNonStandardGainsError, None, Some("formatNonStandardGains"))
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
          (UNPROCESSABLE_ENTITY, "INVALID_DISPOSAL_DATE", INTERNAL_SERVER_ERROR, DownstreamError),
          (UNPROCESSABLE_ENTITY, "INVALID_ACQUISITION_DATE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}