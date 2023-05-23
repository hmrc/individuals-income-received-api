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

package v1andv2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.validations.{DisposalDateErrorMessages, ValueFormatErrorMessages}
import api.models.errors._
import config.AppConfig
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1andv2.models.request.createAmendOtherCgt.CreateAmendOtherCgtRawData

class CreateAmendOtherCgtValidatorSpec extends UnitSpec with ValueFormatErrorMessages with DisposalDateErrorMessages with MockAppConfig {

  object Data {

    val validNino    = "AA123456A"
    val validTaxYear = "2019-20"

    private val validRequestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12,
        |      "gain":1000.12,
        |      "claimOrElectionCodes":[
        |        "PRR"
        |      ],
        |      "gainAfterRelief":1000.12,
        |      "rttTaxPaid":1000.12
        |    }
        |  ],
        |  "nonStandardGains":{
        |    "carriedInterestGain":1000.12,
        |    "carriedInterestRttTaxPaid":1000.12,
        |    "attributedGains":1000.12,
        |    "attributedGainsRttTaxPaid":1000.12,
        |    "otherGains":1000.12,
        |    "otherGainsRttTaxPaid":1000.12
        |  },
        |  "losses":{
        |    "broughtForwardLossesUsedInCurrentYear":1000.12,
        |    "setAgainstInYearGains":1000.12,
        |    "setAgainstInYearGeneralIncome":1000.12,
        |    "setAgainstEarlierYear":1000.12
        |  },
        |  "adjustments":1000.12
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

    private val emptyObjectsJson: JsValue = Json.parse(
      """
        |{
        |  "disposals": [],
        |  "nonStandardGains": {},
        |  "losses": {}
        |}
    """.stripMargin
    )

    private val missingMandatoryFieldJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[{}]
        |}
    """.stripMargin
    )

    private val gainAndLossJson: JsValue = Json.parse(
      """
        |{
        |  "disposals": [
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12,
        |      "gain":1000.12,
        |      "loss":1000.12
        |    }
        |  ]
        |}
    """.stripMargin
    )

    private val oneBadValueFieldJson: JsValue = Json.parse(
      """
        |{
        |  "disposals": [
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.123
        |    }
        |  ]
        |}
    """.stripMargin
    )

    private val badAdjustmentsJson: JsValue = Json.parse(
      """
        |{
        |  "adjustments": 1000.123
        |}
    """.stripMargin
    )

    private val allBadValueFieldsWithGainsJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":678987654567898654567876545678765456789765456789876545678987671000.13,
        |      "allowableCosts":-1000.12,
        |      "gain":1000.122,
        |      "claimOrElectionCodes":[
        |        "PRR"
        |      ],
        |      "gainAfterRelief":1000.612,
        |      "rttTaxPaid":1000.882
        |    }
        |  ],
        |  "nonStandardGains":{
        |    "carriedInterestGain":1000.712,
        |    "carriedInterestRttTaxPaid":-1000.12,
        |    "attributedGains":-1000.12,
        |    "attributedGainsRttTaxPaid":1000.712,
        |    "otherGains":1000.152,
        |    "otherGainsRttTaxPaid":1000.512
        |  },
        |  "losses":{
        |    "broughtForwardLossesUsedInCurrentYear":1000.412,
        |    "setAgainstInYearGains":1000.172,
        |    "setAgainstInYearGeneralIncome":1000.612,
        |    "setAgainstEarlierYear":1000.152
        |  },
        |  "adjustments":1000.142
        |}
    """.stripMargin
    )

    private val allBadValueFieldsWithLossesJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":678987654567898654567876545678765456789765456789876545678987671000.13,
        |      "allowableCosts":-1000.12,
        |      "loss":1000.122,
        |      "claimOrElectionCodes":[
        |        "PRR"
        |      ],
        |      "lossAfterRelief":1000.612,
        |      "rttTaxPaid":1000.882
        |    }
        |  ],
        |  "nonStandardGains":{
        |    "carriedInterestGain":1000.712,
        |    "carriedInterestRttTaxPaid":-1000.12,
        |    "attributedGains":-1000.12,
        |    "attributedGainsRttTaxPaid":1000.712,
        |    "otherGains":1000.152,
        |    "otherGainsRttTaxPaid":1000.512
        |  },
        |  "losses":{
        |    "broughtForwardLossesUsedInCurrentYear":1000.412,
        |    "setAgainstInYearGains":1000.172,
        |    "setAgainstInYearGeneralIncome":1000.612,
        |    "setAgainstEarlierYear":1000.152
        |  },
        |  "adjustments":1000.142
        |}
    """.stripMargin
    )

    private val allBadValueFieldsMultipleDisposalsJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":678987654567898654567876545678765456789765456789876545678987671000.13,
        |      "allowableCosts":-1000.12,
        |      "gain":1000.122,
        |      "claimOrElectionCodes":[
        |        "PRR"
        |      ],
        |      "gainAfterRelief":1000.612,
        |      "rttTaxPaid":1000.882
        |    },
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":678987654567898654567876545678765456789765456789876545678987671000.13,
        |      "allowableCosts":-1000.12,
        |      "loss":1000.122,
        |      "claimOrElectionCodes":[
        |        "PRR"
        |      ],
        |      "lossAfterRelief":1000.612,
        |      "rttTaxPaid":1000.882
        |    }
        |  ],
        |  "nonStandardGains":{
        |    "carriedInterestGain":1000.712,
        |    "carriedInterestRttTaxPaid":-1000.12,
        |    "attributedGains":-1000.12,
        |    "attributedGainsRttTaxPaid":1000.712,
        |    "otherGains":1000.152,
        |    "otherGainsRttTaxPaid":1000.512
        |  },
        |  "losses":{
        |    "broughtForwardLossesUsedInCurrentYear":1000.412,
        |    "setAgainstInYearGains":1000.172,
        |    "setAgainstInYearGeneralIncome":1000.612,
        |    "setAgainstEarlierYear":1000.152
        |  },
        |  "adjustments":1000.142
        |}
    """.stripMargin
    )

    private val badDateJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"20210501",
        |      "disposalDate":"20210601",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val badAssetDescriptionJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val badAssetTypeJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"beans",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val badClaimOrElectionCodesJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12,
        |      "claimOrElectionCodes": [
        |        "beans",
        |        "eggs",
        |        "sausages",
        |        "toast",
        |        "PRR"
        |      ]
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val badDisposalDateJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2010-05-01",
        |      "disposalDate":"2010-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val badAcquisitionDateJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-07-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val badGainAfterReliefLossAfterReliefJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12,
        |      "lossAfterRelief":1000.12,
        |      "gainAfterRelief":1000.12
        |    }
        |  ]
        |}
        |""".stripMargin
    )

    private val allThreeGainsMissingJson: JsValue = Json.parse(
      """
        |{
        |  "disposals":[
        |    {
        |      "assetType":"otherProperty",
        |      "assetDescription":"Property Sale",
        |      "acquisitionDate":"2019-05-01",
        |      "disposalDate":"2019-06-01",
        |      "disposalProceeds":1000.12,
        |      "allowableCosts":1000.12,
        |      "gain":1000.12,
        |      "claimOrElectionCodes":[
        |        "PRR"
        |      ],
        |      "gainAfterRelief":1000.12,
        |      "rttTaxPaid":1000.12
        |    }
        |  ],
        |  "nonStandardGains":{
        |    "carriedInterestRttTaxPaid":1000.12,
        |    "attributedGainsRttTaxPaid":1000.12,
        |    "otherGainsRttTaxPaid":1000.12
        |  },
        |  "losses":{
        |    "broughtForwardLossesUsedInCurrentYear":1000.12,
        |    "setAgainstInYearGains":1000.12,
        |    "setAgainstInYearGeneralIncome":1000.12,
        |    "setAgainstEarlierYear":1000.12
        |  },
        |  "adjustments":1000.12
        |}
    """.stripMargin
    )

    val validRawRequestBody: AnyContentAsJson                              = AnyContentAsJson(validRequestBodyJson)
    val emptyRawRequestBody: AnyContentAsJson                              = AnyContentAsJson(emptyRequestBodyJson)
    val nonsenseRawRequestBody: AnyContentAsJson                           = AnyContentAsJson(nonsenseRequestBodyJson)
    val nonValidRawRequestBody: AnyContentAsJson                           = AnyContentAsJson(nonValidRequestBodyJson)
    val missingMandatoryFieldRawRequestBody: AnyContentAsJson              = AnyContentAsJson(missingMandatoryFieldJson)
    val emptyObjectsRawRequestBody: AnyContentAsJson                       = AnyContentAsJson(emptyObjectsJson)
    val gainAndLossRawRequestBody: AnyContentAsJson                        = AnyContentAsJson(gainAndLossJson)
    val oneBadValueFieldRawRequestBody: AnyContentAsJson                   = AnyContentAsJson(oneBadValueFieldJson)
    val badAdjustmentsRawRequestBody: AnyContentAsJson                     = AnyContentAsJson(badAdjustmentsJson)
    val allBadValueFieldsWithGainsRawRequestBody: AnyContentAsJson         = AnyContentAsJson(allBadValueFieldsWithGainsJson)
    val allBadValueFieldsWithLossesRawRequestBody: AnyContentAsJson        = AnyContentAsJson(allBadValueFieldsWithLossesJson)
    val allBadValueFieldsMultipleDisposalsRawRequestBody: AnyContentAsJson = AnyContentAsJson(allBadValueFieldsMultipleDisposalsJson)
    val badDateRawRequestBody: AnyContentAsJson                            = AnyContentAsJson(badDateJson)
    val badAssetDescriptionRawRequestBody: AnyContentAsJson                = AnyContentAsJson(badAssetDescriptionJson)
    val badAssetTypeRawRequestBody: AnyContentAsJson                       = AnyContentAsJson(badAssetTypeJson)
    val badClaimOrElectionCodesRawRequestBody: AnyContentAsJson            = AnyContentAsJson(badClaimOrElectionCodesJson)
    val badDisposalDateRawRequestBody: AnyContentAsJson                    = AnyContentAsJson(badDisposalDateJson)
    val badAcquisitionDateRawRequestBody: AnyContentAsJson                 = AnyContentAsJson(badAcquisitionDateJson)
    val badGainAfterReliefLossAfterReliefRawRequestBody: AnyContentAsJson  = AnyContentAsJson(badGainAfterReliefLossAfterReliefJson)
    val allThreeGainsMissingRequestBody: AnyContentAsJson                  = AnyContentAsJson(allThreeGainsMissingJson)
  }

  import Data._

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new CreateAmendOtherCgtValidator()

    private val MINIMUM_YEAR = 2020

    MockedAppConfig.minimumPermittedTaxYear returns MINIMUM_YEAR
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(CreateAmendOtherCgtRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, "20178", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, "2017-18", validRawRequestBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, "2019-23", validRawRequestBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(Seq(
                "/disposals/0/acquisitionDate",
                "/disposals/0/allowableCosts",
                "/disposals/0/assetDescription",
                "/disposals/0/assetType",
                "/disposals/0/disposalDate",
                "/disposals/0/disposalProceeds"
              ))
            ))
      }

      "the submitted request body contains empty objects" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, emptyObjectsRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(
                Seq(
                  "/disposals",
                  "/nonStandardGains",
                  "/losses"
                ))))
      }

      "the submitted request body has missing mandatory fields" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, missingMandatoryFieldRawRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq(
              "/disposals/0/acquisitionDate",
              "/disposals/0/allowableCosts",
              "/disposals/0/assetDescription",
              "/disposals/0/assetType",
              "/disposals/0/disposalDate",
              "/disposals/0/disposalProceeds"
            ))))
      }

      "the submitted body is missing carriedInterestGain, attributedGains, and otherGains " in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, allThreeGainsMissingRequestBody)) shouldBe
          List(
            RuleIncorrectOrEmptyBodyError.copy(paths = Some(
              Seq(
                "/nonStandardGains"
              )))
          )
      }
    }

    "return RuleGainLossError error" when {
      "gain and loss fields are both supplied" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, gainAndLossRawRequestBody)) shouldBe
          List(RuleGainLossError.copy(paths = Some(List("/disposals/0"))))
      }
    }

    "return ValueFormatError error" when {
      "one field fails value validation" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, oneBadValueFieldRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/disposals/0/allowableCosts"))
            ))
      }

      "adjustments fails value validation" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, badAdjustmentsRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = BIG_DECIMAL_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/adjustments"))
            ))
      }

      "all fields fail value validation (gains)" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, allBadValueFieldsWithGainsRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
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
            ),
            ValueFormatError.copy(
              message = BIG_DECIMAL_MINIMUM_INCLUSIVE,
              paths = Some(
                Seq(
                  "/adjustments"
                ))
            )
          )
      }

      "all fields fail value validation (losses)" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, allBadValueFieldsWithLossesRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq(
                "/disposals/0/disposalProceeds",
                "/disposals/0/allowableCosts",
                "/disposals/0/loss",
                "/disposals/0/lossAfterRelief",
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
            ),
            ValueFormatError.copy(
              message = BIG_DECIMAL_MINIMUM_INCLUSIVE,
              paths = Some(
                Seq(
                  "/adjustments"
                ))
            )
          )
      }

      "all fields fail value validation (multiple disposals)" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, allBadValueFieldsMultipleDisposalsRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq(
                "/disposals/0/disposalProceeds",
                "/disposals/0/allowableCosts",
                "/disposals/0/gain",
                "/disposals/0/gainAfterRelief",
                "/disposals/0/rttTaxPaid",
                "/disposals/1/disposalProceeds",
                "/disposals/1/allowableCosts",
                "/disposals/1/loss",
                "/disposals/1/lossAfterRelief",
                "/disposals/1/rttTaxPaid",
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
            ),
            ValueFormatError.copy(
              message = BIG_DECIMAL_MINIMUM_INCLUSIVE,
              paths = Some(
                Seq(
                  "/adjustments"
                ))
            )
          )
      }
    }

    "return DateFormatError error" when {
      "supplied dates are invalid" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, badDateRawRequestBody)) shouldBe
          List(
            DateFormatError.copy(
              paths = Some(
                Seq(
                  "/disposals/0/acquisitionDate",
                  "/disposals/0/disposalDate"
                ))
            ))
      }
    }

    "return AssetDescriptionFormatError error" when {
      "supplied asset description is invalid" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, badAssetDescriptionRawRequestBody)) shouldBe
          List(
            AssetDescriptionFormatError.copy(
              paths = Some(
                Seq(
                  "/disposals/0/assetDescription"
                ))
            ))
      }
    }

    "return AssetTypeFormatError error" when {
      "supplied asset type is invalid" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, badAssetTypeRawRequestBody)) shouldBe
          List(
            AssetTypeFormatError.copy(
              paths = Some(
                Seq(
                  "/disposals/0/assetType"
                ))
            ))
      }
    }

    "return ClaimOrElectionCodesFormatError error" when {
      "supplied claimOrElectionCodes are invalid" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, badClaimOrElectionCodesRawRequestBody)) shouldBe
          List(
            ClaimOrElectionCodesFormatError.copy(
              paths = Some(Seq(
                "/disposals/0/claimOrElectionCodes/0",
                "/disposals/0/claimOrElectionCodes/1",
                "/disposals/0/claimOrElectionCodes/2",
                "/disposals/0/claimOrElectionCodes/3"
              ))
            ))
      }
    }

    "return RuleGainAfterReliefLossAfterReliefError error" when {
      "both gainAfterRelief and lossAfterRelief are supplied" in new Test {
        validator.validate(CreateAmendOtherCgtRawData(validNino, validTaxYear, badGainAfterReliefLossAfterReliefRawRequestBody)) shouldBe
          List(
            RuleGainAfterReliefLossAfterReliefError.copy(
              paths = Some(
                Seq(
                  "/disposals/0"
                ))
            ))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validator.validate(CreateAmendOtherCgtRawData("A12344A", "20178", validRawRequestBody)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }

}
