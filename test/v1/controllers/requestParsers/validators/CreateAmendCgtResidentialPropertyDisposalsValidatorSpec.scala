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

package v1.controllers.requestParsers.validators

import config.AppConfig
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import utils.CurrentDateTime
import v1.controllers.requestParsers.validators.validations.{DisposalDateErrorMessages, ValueFormatErrorMessages}
import v1.mocks.MockCurrentDateTime
import v1.models.errors._
import v1.models.request.createAmendCgtResidentialPropertyDisposals.CreateAmendCgtResidentialPropertyDisposalsRawData

import java.time.LocalDate

class CreateAmendCgtResidentialPropertyDisposalsValidatorSpec
    extends UnitSpec
    with ValueFormatErrorMessages
    with DisposalDateErrorMessages
    with MockAppConfig {

  object Data {

    val validNino    = "AA123456A"
    val validTaxYear = "2019-20"

    private val validCustomerReference = "CGTDISPOSAL01"
    private val validDisposalDate = "2020-03-01"
    private val validCompletionDate = "2020-04-02"
    private val validAcquisitionDate = "2020-04-02"
    private val validValue = 1000.12

    private val validRequestBodyJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue
        |    }
        |  ]
        |}
    """.stripMargin
    )

    private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

    private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

    private val nonValidRequestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "disposals": [
        |    {
        |      "disposalDate": true
        |    }
        |  ]
        |}
    """.stripMargin
    )

    private val emptyArrayJson: JsValue = Json.parse(
      """
        |{
        |  "disposals": []
        |}
    """.stripMargin
    )

    private val missingMandatoryFieldsJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[{}]
        |}
    """.stripMargin
    )

    private val gainAndLossJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals": [
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue,
        |      "amountOfNetGain":$validValue
        |    }
        |  ]
        |}
    """.stripMargin
    )

    private val oneBadValueFieldJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals": [
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":1000.123,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue
        |    }
        |  ]
        |}
    """.stripMargin
    )

    private val allBadValueFieldsWithGainsJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":1000.123,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":1000.123,
        |      "improvementCosts":1000.123,
        |      "additionalCosts":100045678987654345678987654567898765456789.12,
        |      "prfAmount":-1000.12,
        |      "otherReliefAmount":1000.123,
        |      "lossesFromThisYear":1000.123,
        |      "lossesFromPreviousYear":1000.123,
        |      "amountOfNetGain":2000.243
        |    }
        |  ]
        |}
    """.stripMargin
    )

    private val allBadValueFieldsWithLossesJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":1000.123,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":1000.123,
        |      "improvementCosts":1000.123,
        |      "additionalCosts":100045678987654345678987654567898765456789.12,
        |      "prfAmount":-1000.12,
        |      "otherReliefAmount":1000.123,
        |      "lossesFromThisYear":1000.123,
        |      "lossesFromPreviousYear":1000.123,
        |      "amountOfNetLoss":2000.243
        |    }
        |  ]
        |}
    """.stripMargin
    )

    private val allBadValueFieldsMultipleDisposalsJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":1000.123,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":1000.123,
        |      "improvementCosts":1000.123,
        |      "additionalCosts":1000.123,
        |      "prfAmount":1000.123,
        |      "otherReliefAmount":1000.123,
        |      "lossesFromThisYear":1000.123,
        |      "lossesFromPreviousYear":1000.123,
        |      "amountOfNetLoss":2000.243
        |    },
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":1000.123,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":1000.123,
        |      "improvementCosts":1000.123,
        |      "additionalCosts":1000.123,
        |      "prfAmount":1000.123,
        |      "otherReliefAmount":1000.123,
        |      "lossesFromThisYear":1000.123,
        |      "lossesFromPreviousYear":1000.123,
        |      "amountOfNetGain":2000.243
        |    }
        |  ]
        |}
    """.stripMargin
    )

    private val badDateJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"20190601",
        |      "completionDate":"20190801",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"20190701",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val badCustomerReferenceJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val completionDateBeforeDisposalDateJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"2020-03-09",
        |      "completionDate":"2020-03-08",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val acquisitionDateBeforeDisposalDateJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"2020-02-01",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val completionDateInFutureJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"2022-08-01",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val completionDateBefore7thMarchJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"2020-03-06",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val disposalDateNotInTaxYearJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"2018-06-01",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetLoss":$validValue
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val lossFromThisYearGreaterThanGainJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromThisYear":$validValue,
        |      "amountOfNetGain":200.24
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val lossFromPreviousYearGreaterThanGainJson: JsValue = Json.parse(
      s"""
        |{
        |  "disposals":[
        |    {
        |      "customerReference":"$validCustomerReference",
        |      "disposalDate":"$validDisposalDate",
        |      "completionDate":"$validCompletionDate",
        |      "disposalProceeds":$validValue,
        |      "acquisitionDate":"$validAcquisitionDate",
        |      "acquisitionAmount":$validValue,
        |      "improvementCosts":$validValue,
        |      "additionalCosts":$validValue,
        |      "prfAmount":$validValue,
        |      "otherReliefAmount":$validValue,
        |      "lossesFromPreviousYear":$validValue,
        |      "amountOfNetGain":200.24
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    val validRawRequestBody: AnyContentAsJson                               = AnyContentAsJson(validRequestBodyJson)
    val emptyRawRequestBody: AnyContentAsJson                               = AnyContentAsJson(emptyRequestBodyJson)
    val nonsenseRawRequestBody: AnyContentAsJson                            = AnyContentAsJson(nonsenseRequestBodyJson)
    val nonValidRawRequestBody: AnyContentAsJson                            = AnyContentAsJson(nonValidRequestBodyJson)
    val missingMandatoryFieldsRawRequestBody: AnyContentAsJson              = AnyContentAsJson(missingMandatoryFieldsJson)
    val emptyArrayRawRequestBody: AnyContentAsJson                          = AnyContentAsJson(emptyArrayJson)
    val oneBadValueFieldRawRequestBody: AnyContentAsJson                    = AnyContentAsJson(oneBadValueFieldJson)
    val allBadValueFieldsWithGainsRawRequestBody: AnyContentAsJson          = AnyContentAsJson(allBadValueFieldsWithGainsJson)
    val allBadValueFieldsWithLossesRawRequestBody: AnyContentAsJson         = AnyContentAsJson(allBadValueFieldsWithLossesJson)
    val allBadValueFieldsMultipleDisposalsRawRequestBody: AnyContentAsJson  = AnyContentAsJson(allBadValueFieldsMultipleDisposalsJson)
    val badDateRawRequestBody: AnyContentAsJson                             = AnyContentAsJson(badDateJson)
    val badCustomerReferenceRawRequestBody: AnyContentAsJson                = AnyContentAsJson(badCustomerReferenceJson)
    val completionDateBeforeDisposalDateRawRequestBody: AnyContentAsJson    = AnyContentAsJson(completionDateBeforeDisposalDateJson)
    val acquisitionDateBeforeDisposalDateRawRequestBody: AnyContentAsJson   = AnyContentAsJson(acquisitionDateBeforeDisposalDateJson)
    val completionDateInFutureRawRequestBody: AnyContentAsJson              = AnyContentAsJson(completionDateInFutureJson)
    val completionDateBefore7thMarchRawRequestBody: AnyContentAsJson        = AnyContentAsJson(completionDateBefore7thMarchJson)
    val disposalDateNotInTaxYearRawRequestBody: AnyContentAsJson            = AnyContentAsJson(disposalDateNotInTaxYearJson)
    val gainAndLossRawRequestBody: AnyContentAsJson                         = AnyContentAsJson(gainAndLossJson)
    val lossFromThisYearGreaterThanGainRawRequestBody: AnyContentAsJson     = AnyContentAsJson(lossFromThisYearGreaterThanGainJson)
    val lossFromPreviousYearGreaterThanGainRawRequestBody: AnyContentAsJson = AnyContentAsJson(lossFromPreviousYearGreaterThanGainJson)
  }

  import Data._

  class Test extends MockAppConfig with MockCurrentDateTime {

    implicit val appConfig: AppConfig              = mockAppConfig
    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime

    val validator = new CreateAmendCgtResidentialPropertyDisposalsValidator()

    MockCurrentDateTime.getLocalDate
      .returns(LocalDate.parse("2021-07-29"))
      .anyNumberOfTimes()

    private val MINIMUM_YEAR = 2020
    MockedAppConfig.minimumPermittedTaxYear returns MINIMUM_YEAR
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, "20178", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, "2017-18", validRawRequestBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, "2019-23", validRawRequestBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(Seq("/disposals"))
            ))
      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(Seq(
                "/disposals/0/acquisitionAmount",
                "/disposals/0/disposalDate",
                "/disposals/0/completionDate",
                "/disposals/0/acquisitionDate",
                "/disposals/0/disposalProceeds"
              ))
            ))
      }

      "the submitted request body has missing mandatory fields" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, missingMandatoryFieldsRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(
              "/disposals/0/acquisitionAmount",
              "/disposals/0/disposalDate",
              "/disposals/0/completionDate",
              "/disposals/0/acquisitionDate",
              "/disposals/0/disposalProceeds"
            ))))
      }

      "the submitted request body contains empty objects" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, emptyArrayRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(Seq(
                "/disposals"
              ))))
      }
    }

    "return ValueFormatError error" when {
      "one field fails value validation" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, oneBadValueFieldRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/disposals/0/disposalProceeds"))
            ))
      }

      "all fields fail value validation (gains)" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, allBadValueFieldsWithGainsRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq(
                "/disposals/0/disposalProceeds",
                "/disposals/0/acquisitionAmount",
                "/disposals/0/improvementCosts",
                "/disposals/0/additionalCosts",
                "/disposals/0/prfAmount",
                "/disposals/0/otherReliefAmount",
                "/disposals/0/lossesFromThisYear",
                "/disposals/0/lossesFromPreviousYear",
                "/disposals/0/amountOfNetGain"
              ))
            )
          )
      }

      "all fields fail value validation (losses)" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, allBadValueFieldsWithLossesRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq(
                "/disposals/0/disposalProceeds",
                "/disposals/0/acquisitionAmount",
                "/disposals/0/improvementCosts",
                "/disposals/0/additionalCosts",
                "/disposals/0/prfAmount",
                "/disposals/0/otherReliefAmount",
                "/disposals/0/lossesFromThisYear",
                "/disposals/0/lossesFromPreviousYear",
                "/disposals/0/amountOfNetLoss"
              ))
            )
          )
      }

      "all fields fail value validation (multiple disposals)" in new Test {
        validator.validate(
          CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, allBadValueFieldsMultipleDisposalsRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq(
                "/disposals/0/disposalProceeds",
                "/disposals/0/acquisitionAmount",
                "/disposals/0/improvementCosts",
                "/disposals/0/additionalCosts",
                "/disposals/0/prfAmount",
                "/disposals/0/otherReliefAmount",
                "/disposals/0/lossesFromThisYear",
                "/disposals/0/lossesFromPreviousYear",
                "/disposals/0/amountOfNetLoss",
                "/disposals/1/disposalProceeds",
                "/disposals/1/acquisitionAmount",
                "/disposals/1/improvementCosts",
                "/disposals/1/additionalCosts",
                "/disposals/1/prfAmount",
                "/disposals/1/otherReliefAmount",
                "/disposals/1/lossesFromThisYear",
                "/disposals/1/lossesFromPreviousYear",
                "/disposals/1/amountOfNetGain"
              ))
            )
          )
      }
    }

    "return DateFormatError error" when {
      "supplied dates are invalid" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, badDateRawRequestBody)) shouldBe
          List(
            DateFormatError.copy(
              paths = Some(
                Seq(
                  "/disposals/0/disposalDate",
                  "/disposals/0/completionDate",
                  "/disposals/0/acquisitionDate"
                ))
            ))
      }
    }

    "return CustomerRefFormatError error" when {
      "supplied asset description is invalid" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, badCustomerReferenceRawRequestBody)) shouldBe
          List(
            CustomerRefFormatError.copy(
              paths = Some(
                Seq(
                  "/disposals/0"
                ))
            ))
      }
    }

    "return RuleCompletionDateBeforeDisposalDateError error" when {
      "supplied completion date is before supplied disposal date" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, completionDateBeforeDisposalDateRawRequestBody)) shouldBe
          List(
            RuleCompletionDateBeforeDisposalDateError.copy(
              paths = Some(
                Seq(
                  "/disposals/0"
                ))
            ))
      }
    }

    "return RuleAcquisitionDateBeforeDisposalDateError error" when {
      "supplied completion date is before supplied disposal date" in new Test {
        validator.validate(
          CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, acquisitionDateBeforeDisposalDateRawRequestBody)) shouldBe
          List(
            RuleAcquisitionDateBeforeDisposalDateError.copy(
              paths = Some(
                Seq(
                  "/disposals/0"
                ))
            ))
      }
    }

    "return RuleCompletionDateError error" when {
      "supplied completion date is too late" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, completionDateInFutureRawRequestBody)) shouldBe
          List(
            RuleCompletionDateError.copy(
              paths = Some(
                Seq(
                  "/disposals/0"
                ))
            ))
      }
      "supplied completion date before 7th March" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, completionDateBefore7thMarchRawRequestBody)) shouldBe
          List(
            RuleCompletionDateError.copy(
              paths = Some(
                Seq(
                  "/disposals/0"
                ))
            ))
      }
    }

    "return RuleDisposalDateError error" when {
      "supplied disposal date is invalid" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, disposalDateNotInTaxYearRawRequestBody)) shouldBe
          List(
            RuleDisposalDateError.copy(
              paths = Some(
                Seq(
                  "/disposals/0"
                )),
              message = IN_YEAR
            ))
      }
    }

    "return RuleGainLossError error" when {
      "gain and loss fields are both supplied" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, gainAndLossRawRequestBody)) shouldBe
          List(RuleGainLossError.copy(paths = Some(List("/disposals/0"))))
      }
    }

    "return RuleLossesGreaterThanGainError error" when {
      "loss from this year is greater than gain" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, lossFromThisYearGreaterThanGainRawRequestBody)) shouldBe
          List(RuleLossesGreaterThanGainError.copy(paths = Some(List("/disposals/0/lossesFromThisYear"))))
      }
      "loss from previous year is greater than gain" in new Test {
        validator.validate(
          CreateAmendCgtResidentialPropertyDisposalsRawData(validNino, validTaxYear, lossFromPreviousYearGreaterThanGainRawRequestBody)) shouldBe
          List(RuleLossesGreaterThanGainError.copy(paths = Some(List("/disposals/0/lossesFromPreviousYear"))))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validator.validate(CreateAmendCgtResidentialPropertyDisposalsRawData("A12344A", "20178", validRawRequestBody)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }
}
