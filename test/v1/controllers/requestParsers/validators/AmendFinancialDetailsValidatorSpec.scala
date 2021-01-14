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

import com.typesafe.config.ConfigFactory
import config.AppConfig
import mocks.MockAppConfig
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import utils.CurrentDateTime
import v1.controllers.requestParsers.validators.validations.ValueFormatErrorMessages
import v1.mocks.MockCurrentDateTime
import v1.models.errors._
import v1.models.request.amendFinancialDetails.AmendFinancialDetailsRawData

class AmendFinancialDetailsValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino = "AA123456A"
  private val validTaxYear = "2020-21"
  private val validEmploymentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val validRequestJson: JsValue = Json.parse(
    """
      |{
      |    "employment": {
      |        "pay": {
      |            "taxablePayToDate": 3500.75,
      |            "totalTaxToDate": 6782.92,
      |            "tipsAndOtherPayments": 1024.99
      |        },
      |        "deductions": {
      |            "studentLoans": {
      |                "uglDeductionAmount": 13343.45,
      |                "pglDeductionAmount": 24242.56
      |            }
      |        },
      |        "benefitsInKind": {
      |            "accommodation": 455.67,
      |            "assets": 435.54,
      |            "assetTransfer": 24.58,
      |            "beneficialLoan": 33.89,
      |            "car": 3434.78,
      |            "carFuel": 34.56,
      |            "educationalServices": 445.67,
      |            "entertaining": 434.45,
      |            "expenses": 3444.32,
      |            "medicalInsurance": 4542.47,
      |            "telephone": 243.43,
      |            "service": 45.67,
      |            "taxableExpenses": 24.56,
      |            "van": 56.29,
      |            "vanFuel": 14.56,
      |            "mileage": 34.23,
      |            "nonQualifyingRelocationExpenses": 54.62,
      |            "nurseryPlaces": 84.29,
      |            "otherItems": 67.67,
      |            "paymentsOnEmployeesBehalf": 67.23,
      |            "personalIncidentalExpenses": 74.29,
      |            "qualifyingRelocationExpenses": 78.24,
      |            "employerProvidedProfessionalSubscriptions": 84.56,
      |            "employerProvidedServices": 56.34,
      |            "incomeTaxPaidByDirector": 67.34,
      |            "travelAndSubsistence": 56.89,
      |            "vouchersAndCreditCards": 34.90,
      |            "nonCash": 23.89
      |        }
      |    }
      |}
    """.stripMargin
  )

  private val emptyRequestJson: JsValue = JsObject.empty

  private val missingMandatoryEmploymentObjectJson: JsValue = Json.parse("""{"field": "value"}""")

  private val missingMandatoryPayObjectJson: JsValue = Json.parse(
    """
      |{
      |    "employment": {}
      |}
    """.stripMargin
  )

  private val missingMandatoryFieldsJson: JsValue = Json.parse(
    """
      |{
      |    "employment": {
      |        "pay": {
      |            "tipsAndOtherPayments": 1024.99
      |        }
      |    }
      |}
    """.stripMargin
  )

  private val incorrectFormatRequestJson: JsValue = Json.parse(
    """
      |{
      |    "employment": {
      |        "pay": {
      |            "taxablePayToDate": true,
      |            "totalTaxToDate": 6782.92,
      |            "tipsAndOtherPayments": 1024.99
      |        },
      |        "deductions": {
      |            "studentLoans": {
      |                "uglDeductionAmount": []
      |            }
      |        },
      |        "benefitsInKind": {
      |            "accommodation": "false"
      |        }
      |    }
      |}
    """.stripMargin
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "employment": {
      |        "pay": {
      |            "taxablePayToDate": 3500.758,
      |            "totalTaxToDate": 6782.923,
      |            "tipsAndOtherPayments": 1024.994
      |        },
      |        "deductions": {
      |            "studentLoans": {
      |                "uglDeductionAmount": -13343.45,
      |                "pglDeductionAmount": -24242.56
      |            }
      |        },
      |        "benefitsInKind": {
      |            "accommodation": -455.67,
      |            "assets": -435.54,
      |            "assetTransfer": -24.58,
      |            "beneficialLoan": -33.89,
      |            "car": -3434.78,
      |            "carFuel": 34.569,
      |            "educationalServices": 445.677,
      |            "entertaining": 434.458,
      |            "expenses": 3444.324,
      |            "medicalInsurance": 4542.475,
      |            "telephone": 243.436,
      |            "service": -45.67,
      |            "taxableExpenses": -24.56,
      |            "van": -56.29,
      |            "vanFuel": -14.56,
      |            "mileage": -34.23,
      |            "nonQualifyingRelocationExpenses": 54.623,
      |            "nurseryPlaces": 84.294,
      |            "otherItems": 67.676,
      |            "paymentsOnEmployeesBehalf": -67.23,
      |            "personalIncidentalExpenses": -74.29,
      |            "qualifyingRelocationExpenses": 78.244,
      |            "employerProvidedProfessionalSubscriptions": -84.56,
      |            "employerProvidedServices": -56.34,
      |            "incomeTaxPaidByDirector": 67.342,
      |            "travelAndSubsistence": -56.89,
      |            "vouchersAndCreditCards": 34.905,
      |            "nonCash": -23.89
      |        }
      |    }
      |}
    """.stripMargin
  )

  private val validRawBody = AnyContentAsJson(validRequestJson)
  private val emptyRawBody = AnyContentAsJson(emptyRequestJson)
  private val missingMandatoryEmploymentRawRequestBody = AnyContentAsJson(missingMandatoryEmploymentObjectJson)
  private val missingMandatoryPayRawRequestBody = AnyContentAsJson(missingMandatoryPayObjectJson)
  private val missingMandatoryFieldsRawRequestBody = AnyContentAsJson(missingMandatoryFieldsJson)
  private val incorrectFormatRawBody = AnyContentAsJson(incorrectFormatRequestJson)
  private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

  class Test(errorFeatureSwitch: Boolean = true) extends MockCurrentDateTime with MockAppConfig {

    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new AmendFinancialDetailsValidator()

    MockCurrentDateTime.getCurrentDate
      .returns(DateTime.parse("2022-07-11", dateTimeFormatter))
      .anyNumberOfTimes()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)

    MockedAppConfig.featureSwitch.returns(Some(Configuration(ConfigFactory.parseString(
      s"""
         |taxYearNotEndedRule.enabled = $errorFeatureSwitch
      """.stripMargin))))
  }

  "AmendFinancialDetailsValidator" when {
    "running a validation" should {
      "return no errors for a valid request" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, validTaxYear, validEmploymentId, validRawBody)) shouldBe Nil
      }

      "return no errors when config for TaxYearNotEndedError is set to false" in new Test(false) {
        validator.validate(AmendFinancialDetailsRawData(validNino, "2022-23", validEmploymentId, validRawBody)) shouldBe
          List.empty
      }

      // parameter format error scenarios
      "return NinoFormatError error when the supplied NINO is invalid" in new Test {
        validator.validate(AmendFinancialDetailsRawData("A12344A", validTaxYear, validEmploymentId, validRawBody)) shouldBe
          List(NinoFormatError)
      }

      "return TaxYearFormatError error for an invalid tax year format" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, "20178", validEmploymentId, validRawBody)) shouldBe
          List(TaxYearFormatError)
      }

      "return RuleTaxYearRangeInvalidError error for an invalid tax year range" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, "2018-20", validEmploymentId, validRawBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }

      "return EmploymentIdFormatError error for an invalid employment id" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, validTaxYear, "notValid", validRawBody)) shouldBe
          List(EmploymentIdFormatError)
      }

      "return multiple errors for multiple invalid request parameters" in new Test {
        validator.validate(AmendFinancialDetailsRawData("notValid", "2018-20", "invalid", validRawBody)) shouldBe
          List(NinoFormatError, RuleTaxYearRangeInvalidError, EmploymentIdFormatError)
      }

      // parameter rule error scenarios
      "return RuleTaxYearNotSupportedError error for an unsupported tax year" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, "2019-20", validEmploymentId, validRawBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }

      "return RuleTaxYearNotEndedError error for a tax year which hasn't ended" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, "2022-23", validEmploymentId, validRawBody)) shouldBe
          List(RuleTaxYearNotEndedError)
      }

      // body format error scenarios
      "return RuleIncorrectOrEmptyBodyError error for an empty request body" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, validTaxYear, validEmploymentId, emptyRawBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "return RuleIncorrectOrEmptyBodyError error for a non-empty JSON body with no expected fields provided (No mandatory employment object)" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, validTaxYear, validEmploymentId, missingMandatoryEmploymentRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/employment"))))
      }

      "return RuleIncorrectOrEmptyBodyError error for a non-empty JSON body with no mandatory pay object provided" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, validTaxYear, validEmploymentId, missingMandatoryPayRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/employment/pay"))))
      }

      "return RuleIncorrectOrEmptyBodyError error when mandatory fields are not provided" in new Test {
        val paths: Seq[String] = Seq("/employment/pay/taxablePayToDate", "/employment/pay/totalTaxToDate")

        validator.validate(AmendFinancialDetailsRawData(validNino, validTaxYear, validEmploymentId, missingMandatoryFieldsRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(paths)))
      }

      "return RuleIncorrectOrEmptyBodyError error for an incorrect formatted request body" in new Test {
        val paths: Seq[String] = Seq(
          "/employment/pay/taxablePayToDate",
          "/employment/benefitsInKind/accommodation",
          "/employment/deductions/studentLoans/uglDeductionAmount"
        )

        validator.validate(AmendFinancialDetailsRawData(validNino, validTaxYear, validEmploymentId, incorrectFormatRawBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(paths)))
      }

      // body value error scenarios
      "return ValueFormatError error for incorrect field formats" in new Test {
        validator.validate(AmendFinancialDetailsRawData(validNino, validTaxYear, validEmploymentId, allInvalidValueRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = BIG_DECIMAL_MINIMUM_INCLUSIVE,
              paths = Some(List("/employment/pay/totalTaxToDate"))
            ),
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(List(
                "/employment/pay/taxablePayToDate",
                "/employment/pay/tipsAndOtherPayments",
                "/employment/deductions/studentLoans/uglDeductionAmount",
                "/employment/deductions/studentLoans/pglDeductionAmount",
                "/employment/benefitsInKind/accommodation",
                "/employment/benefitsInKind/assets",
                "/employment/benefitsInKind/assetTransfer",
                "/employment/benefitsInKind/beneficialLoan",
                "/employment/benefitsInKind/car",
                "/employment/benefitsInKind/carFuel",
                "/employment/benefitsInKind/educationalServices",
                "/employment/benefitsInKind/entertaining",
                "/employment/benefitsInKind/expenses",
                "/employment/benefitsInKind/medicalInsurance",
                "/employment/benefitsInKind/telephone",
                "/employment/benefitsInKind/service",
                "/employment/benefitsInKind/taxableExpenses",
                "/employment/benefitsInKind/van",
                "/employment/benefitsInKind/vanFuel",
                "/employment/benefitsInKind/mileage",
                "/employment/benefitsInKind/nonQualifyingRelocationExpenses",
                "/employment/benefitsInKind/nurseryPlaces",
                "/employment/benefitsInKind/otherItems",
                "/employment/benefitsInKind/paymentsOnEmployeesBehalf",
                "/employment/benefitsInKind/personalIncidentalExpenses",
                "/employment/benefitsInKind/qualifyingRelocationExpenses",
                "/employment/benefitsInKind/employerProvidedProfessionalSubscriptions",
                "/employment/benefitsInKind/employerProvidedServices",
                "/employment/benefitsInKind/incomeTaxPaidByDirector",
                "/employment/benefitsInKind/travelAndSubsistence",
                "/employment/benefitsInKind/vouchersAndCreditCards",
                "/employment/benefitsInKind/nonCash"
                ))
            )
          )
      }
    }
  }
}