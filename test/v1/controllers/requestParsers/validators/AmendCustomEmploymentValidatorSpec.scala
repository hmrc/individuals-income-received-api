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
import v1.models.request.amendCustomEmployment.AmendCustomEmploymentRawData

class AmendCustomEmploymentValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino = "AA123456A"
  private val validTaxYear = "2020-21"
  private val validEmploymentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val validRequestJson: JsValue = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412"
      |}
    """.stripMargin
  )

  private val emptyRequestJson: JsValue = JsObject.empty

  private val incorrectFormatRequestJson: JsValue = Json.parse(
    """
      |{
      |  "employerRef": true,
      |  "cessationDate": 400,
      |  "payrollId": []
      |}
    """.stripMargin
  )

  private val invalidValueRequestJson: JsValue = Json.parse(
    s"""
      |{
      |  "employerRef": "notValid",
      |  "employerName": "${"a"*75}",
      |  "startDate": "notValid",
      |  "cessationDate": "notValid",
      |  "payrollId": "${"b"*75}"
      |}
    """.stripMargin
  )

  private val invalidDatesRequestJson1: JsValue = Json.parse(
    s"""
        |{
        |  "employerRef": "123/AZ12334",
        |  "employerName": "AMD infotech Ltd",
        |  "startDate": "2019-01-01",
        |  "cessationDate": "2018-06-01",
        |  "payrollId": "124214112412"
        |}
    """.stripMargin
  )

  private val invalidDatesRequestJson2: JsValue = Json.parse(
    s"""
       |{
       |  "employerRef": "123/AZ12334",
       |  "employerName": "AMD infotech Ltd",
       |  "startDate": "2023-01-01",
       |  "cessationDate": "2022-06-01",
       |  "payrollId": "124214112412"
       |}
    """.stripMargin
  )

  private val validRawBody = AnyContentAsJson(validRequestJson)
  private val emptyRawBody = AnyContentAsJson(emptyRequestJson)
  private val incorrectFormatRawBody = AnyContentAsJson(incorrectFormatRequestJson)
  private val incorrectValueRawBody = AnyContentAsJson(invalidValueRequestJson)
  private val incorrectDatesRawBody1 = AnyContentAsJson(invalidDatesRequestJson1)
  private val incorrectDatesRawBody2 = AnyContentAsJson(invalidDatesRequestJson2)

  class Test(errorFeatureSwitch: Boolean = true) extends MockCurrentDateTime with MockAppConfig {

    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new AmendCustomEmploymentValidator()

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

  "AmendCustomEmploymentValidator" when {
    "running a validation" should {
      "return no errors for a valid request" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, validTaxYear, validEmploymentId, validRawBody)) shouldBe Nil
      }

      "return RuleCessationDateBeforeTaxYearStart error when config for TaxYearNotEndedError is set to false" in new Test(false) {
        validator.validate(AmendCustomEmploymentRawData(validNino, "2022-23", validEmploymentId, validRawBody)) shouldBe List(RuleCessationDateBeforeTaxYearStartError)
      }

      // parameter format error scenarios
      "return NinoFormatError error when the supplied NINO is invalid" in new Test {
        validator.validate(AmendCustomEmploymentRawData("A12344A", validTaxYear, validEmploymentId, validRawBody)) shouldBe
          List(NinoFormatError)
      }

      "return TaxYearFormatError error for an invalid tax year format" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, "20178", validEmploymentId, validRawBody)) shouldBe
          List(TaxYearFormatError)
      }

      "return RuleTaxYearRangeInvalidError error for an invalid tax year range" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, "2018-20", validEmploymentId, validRawBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }

      "return EmploymentIdFormatError error for an invalid employment id" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, validTaxYear, "notValid", validRawBody)) shouldBe
          List(EmploymentIdFormatError)
      }

      "return multiple errors for multiple invalid request parameters" in new Test {
        validator.validate(AmendCustomEmploymentRawData("notValid", "2018-20", "invalid", validRawBody)) shouldBe
          List(NinoFormatError, RuleTaxYearRangeInvalidError, EmploymentIdFormatError)
      }

      // parameter rule error scenarios
      "return RuleTaxYearNotSupportedError error for an unsupported tax year" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, "2019-20", validEmploymentId, validRawBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }

      "return RuleTaxYearNotEndedError error for a tax year which hasn't ended" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, "2022-23", validEmploymentId, validRawBody)) shouldBe
          List(RuleTaxYearNotEndedError)
      }

      // body format error scenarios
      "return RuleIncorrectOrEmptyBodyError error for an empty request body" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, validTaxYear, validEmploymentId, emptyRawBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "return RuleIncorrectOrEmptyBodyError error for an incorrect request body" in new Test {
        val paths = List("/employerRef", "/employerName", "/payrollId", "/cessationDate", "/startDate")

        validator.validate(AmendCustomEmploymentRawData(validNino, validTaxYear, validEmploymentId, incorrectFormatRawBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(paths)))
      }

      // body value error scenarios
      "return multiple errors for incorrect field formats" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, validTaxYear, validEmploymentId, incorrectValueRawBody)) shouldBe
          List(EmployerRefFormatError, EmployerNameFormatError, StartDateFormatError, CessationDateFormatError, PayrollIdFormatError)
      }

      "return multiple errors for dates which precede the current tax year and are incorrectly ordered" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, validTaxYear, validEmploymentId, incorrectDatesRawBody1)) shouldBe
          List(RuleCessationDateBeforeTaxYearStartError, RuleCessationDateBeforeStartDateError)
      }

      "return multiple errors for dates which exceed the current tax year and are incorrectly ordered" in new Test {
        validator.validate(AmendCustomEmploymentRawData(validNino, validTaxYear, validEmploymentId, incorrectDatesRawBody2)) shouldBe
          List(RuleStartDateAfterTaxYearEndError, RuleCessationDateBeforeStartDateError)
      }
    }
  }
}