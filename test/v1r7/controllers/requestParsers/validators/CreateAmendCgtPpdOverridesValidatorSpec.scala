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

package v1r7.controllers.requestParsers.validators

import config.AppConfig
import mocks.MockAppConfig
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import utils.CurrentDateTime
import v1r7.controllers.requestParsers.validators.validations.ValueFormatErrorMessages
import v1r7.mocks.MockCurrentDateTime
import v1r7.models.errors._
import v1r7.models.request.createAmendCgtPpdOverrides.CreateAmendCgtPpdOverridesRawData

class CreateAmendCgtPpdOverridesValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino = "AA123456A"
  private val validTaxYear = "2019-20"

  private val validRequestJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.78
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098",
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
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
      |             "ppdSubmissionId": "AB0000000091",
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

  private val validOnlyMultiplePropertyDisposalsRequestJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.78
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098",
      |            "amountOfNetLoss": 134.99
      |         }
      |    ]
      |}
      |""".stripMargin
  )

  private val validOnlySinglePropertyDisposalsRequestJson: JsValue = Json.parse(
    """
      |{
      |   "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
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
      |             "ppdSubmissionId": "AB0000000091",
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

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "amountOfNetGain": 1234.78
      |         },
      |         {
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
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

  private val emptyMultiplePropertyDisposalsRequestJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
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
      |             "ppdSubmissionId": "AB0000000091",
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

  private val emptySinglePropertyDisposalsRequestJson: JsValue = Json.parse(
    """
      |{
      |    "singlePropertyDisposals": []
      |}
      |""".stripMargin
  )

  private val invalidSubmissionIdRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "notAnID",
      |            "amountOfNetGain": 1234.78
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098",
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
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
      |             "ppdSubmissionId": "AB0000000091",
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

  private val invalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.787385
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetLoss": -134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000092",
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
      |             "ppdSubmissionId": "AB0000000092",
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

  private val invalidDateRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.78
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098",
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
      |             "completionDate": "20-02-28",
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
      |             "ppdSubmissionId": "AB0000000091",
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

  private val bothGainsAndLossMultiplePropertyDisposalsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.78,
      |            "amountOfNetLoss": 134.99
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098",
      |            "amountOfNetGain": 1234.78,
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
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
      |             "ppdSubmissionId": "AB0000000091",
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

  private val bothGainsAndLossSinglePropertyDisposalsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetLoss": 134.99
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098",
      |            "amountOfNetGain": 1234.78
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
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
      |             "amountOfNetLoss": 4567.89
      |         },
      |         {
      |             "ppdSubmissionId": "AB0000000091",
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
      |             "amountOfNetLoss": 4567.89
      |         }
      |    ]
      |}
      |""".stripMargin
  )

  private val neitherGainsOrLossMultiplePropertyDisposalsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092"
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098"
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
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
      |             "ppdSubmissionId": "AB0000000091",
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

  private val neitherGainsOrLossSinglePropertyDisposalsRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetLoss": 134.99
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098",
      |            "amountOfNetGain": 1234.78
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23
      |         },
      |         {
      |             "ppdSubmissionId": "AB0000000091",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23
      |         }
      |    ]
      |}
      |""".stripMargin
  )

  private val currentYearLossesGreaterThanGainsJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.78
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098",
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
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
      |             "amountOfNetGain": 67.89
      |         },
      |         {
      |             "ppdSubmissionId": "AB0000000091",
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

  private val validRequestBody = AnyContentAsJson(validRequestJson)
  private val missingMandatoryFieldRequestBody = AnyContentAsJson(missingMandatoryFieldJson)
  private val emptyMultiplePropertyDisposalsRequestBody = AnyContentAsJson(emptyMultiplePropertyDisposalsRequestJson)
  private val emptySinglePropertyDisposalsRequestBody = AnyContentAsJson(emptySinglePropertyDisposalsRequestJson)
  private val invalidSubmissionIdRequestBody = AnyContentAsJson(invalidSubmissionIdRequestBodyJson)
  private val invalidValueRequestBody = AnyContentAsJson(invalidValueRequestBodyJson)
  private val invalidDateRequestBody = AnyContentAsJson(invalidDateRequestBodyJson)
  private val bothGainsAndLossMultiplePropertyDisposalsRequestBody = AnyContentAsJson(bothGainsAndLossMultiplePropertyDisposalsRequestBodyJson)
  private val bothGainsAndLossSinglePropertyDisposalsRequestBody = AnyContentAsJson(bothGainsAndLossSinglePropertyDisposalsRequestBodyJson)
  private val neitherGainsOrLossMultiplePropertyDisposalsRequestBody = AnyContentAsJson(neitherGainsOrLossMultiplePropertyDisposalsRequestBodyJson)
  private val neitherGainsOrLossSinglePropertyDisposalsRequestBody = AnyContentAsJson(neitherGainsOrLossSinglePropertyDisposalsRequestBodyJson)
  private val currentYearLossesGreaterThanGainsRequestBody = AnyContentAsJson(currentYearLossesGreaterThanGainsJson)


  class Test extends MockCurrentDateTime with MockAppConfig {

    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new CreateAmendCgtPpdOverridesValidator()

    MockCurrentDateTime.getDateTime
      .returns(DateTime.parse("2021-07-11", dateTimeFormatter))
      .anyNumberOfTimes()

    private val MINIMUM_PERMITTED_TAX_YEAR = 2020

    MockedAppConfig.minimumPermittedTaxYear
      .returns(MINIMUM_PERMITTED_TAX_YEAR)
      .anyNumberOfTimes()
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, validRequestBody)) shouldBe Nil
      }

      "a valid request contains only multiple disposals is supplied" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear,
          AnyContentAsJson(validOnlyMultiplePropertyDisposalsRequestJson))) shouldBe Nil
      }

      "a valid request contains only single disposals is supplied" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear,
          AnyContentAsJson(validOnlySinglePropertyDisposalsRequestJson))) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData("A12344A", validTaxYear, validRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, "20178", validRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return a RuleTaxYearNotEnded error" when {
      "the current tax year is provided" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, "2021-22", validRequestBody)) shouldBe
          List(RuleTaxYearNotEndedError)
      }
    }

    "return a RuleTaxYearRangeInvalidError" when {
      "a tex year with an invalid range is provided" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, "2018-20", validRequestBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleTaxYearNotSupported error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, "2017-18", validRequestBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an JSON body missing a mandatory field is submitted" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, missingMandatoryFieldRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(
            paths = Some(Seq("/multiplePropertyDisposals/0/ppdSubmissionId",
              "/multiplePropertyDisposals/1/ppdSubmissionId",
              "/singlePropertyDisposals/0/ppdSubmissionId",
              "/singlePropertyDisposals/1/ppdSubmissionId"))))
      }

      "an JSON body with empty multiplePropertyDisposals array is submitted" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, emptyMultiplePropertyDisposalsRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/multiplePropertyDisposals"))))
      }

      "an JSON body with empty singlePropertyDisposals array is submitted" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, emptySinglePropertyDisposalsRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/singlePropertyDisposals"))))
      }
    }

    "return a PpdSubmissionIdFormatError" when {
      "a body with incorrect ppdSubmissionIds is submitted" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, invalidSubmissionIdRequestBody)) shouldBe
          List(PpdSubmissionIdFormatError.copy(paths = Some(Seq("/multiplePropertyDisposals/0/ppdSubmissionId"))))
      }
    }

    "return a valueFormatError" when {
      "a body with incorrect values is submitted" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, invalidValueRequestBody)) shouldBe
          List(ValueFormatError.copy(
            paths = Some(List(
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
            )),
            message = "The value must be between 0 and 99999999999.99"
          )
          )
      }
    }

    "return a dateFormatError" when {
      "a body with an incorrect date is provided" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, invalidDateRequestBody)) shouldBe
          List(DateFormatError.copy(paths = Some(Seq("/singlePropertyDisposals/0/completionDate"))))
      }
    }

    "return a RuleAmountGainLossError" when {
      "both amountOfNetGain and amountOfNetLoss are provided for multiplePropertyDisposals" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, bothGainsAndLossMultiplePropertyDisposalsRequestBody)) shouldBe
          List(RuleAmountGainLossError.copy(paths = Some(Seq("/multiplePropertyDisposals/0", "/multiplePropertyDisposals/1"))))
      }

      "neither amountOfNetGain or amountOfNetLoss are provided for multiplePropertyDisposals" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, neitherGainsOrLossMultiplePropertyDisposalsRequestBody)) shouldBe
          List(RuleAmountGainLossError.copy(paths = Some(Seq("/multiplePropertyDisposals/0", "/multiplePropertyDisposals/1"))))
      }

      "both amountOfNetGain and amountOfNetLoss are provided for singlePropertyDisposals" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, bothGainsAndLossSinglePropertyDisposalsRequestBody)) shouldBe
          List(RuleAmountGainLossError.copy(paths = Some(Seq("/singlePropertyDisposals/0", "/singlePropertyDisposals/1"))))
      }

      "neither amountOfNetGain or amountOfNetLoss are provided for singlePropertyDisposals" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, neitherGainsOrLossSinglePropertyDisposalsRequestBody)) shouldBe
          List(RuleAmountGainLossError.copy(paths = Some(Seq("/singlePropertyDisposals/0", "/singlePropertyDisposals/1"))))
      }
    }

    "return a RuleLossesGreaterThanGainError" when {
      "the losses for this year are larger than the total gains" in new Test {
        validator.validate(CreateAmendCgtPpdOverridesRawData(validNino, validTaxYear, currentYearLossesGreaterThanGainsRequestBody)) shouldBe
          List(RuleLossesGreaterThanGainError.copy(paths = Some(Seq(
            "/singlePropertyDisposals/0/lossesFromThisYear",
            "/singlePropertyDisposals/0/lossesFromPreviousYear"
          ))))
      }
    }
  }
}
