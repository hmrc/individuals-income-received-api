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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.validations.ValueFormatErrorMessages
import api.mocks.MockCurrentDateTime
import api.models.errors._
import config.AppConfig
import mocks.MockAppConfig
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import utils.CurrentDateTime
import v1.models.request.addCustomEmployment.AddCustomEmploymentRawData

class AddCustomEmploymentValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

  private def requestJsonWithDates(startDate: String = "2019-01-01", cessationDate: String = "2020-06-01") = AnyContentAsJson(Json.parse(s"""
       |{
       |  "employerRef": "123/AZ12334",
       |  "employerName": "AMD infotech Ltd",
       |  "startDate": "$startDate",
       |  "cessationDate": "$cessationDate",
       |  "payrollId": "124214112412",
       |  "occupationalPension": false
       |}
  """.stripMargin))

  private val validRawBody = requestJsonWithDates()

  private val emptyRawBody = AnyContentAsJson(JsObject.empty)

  private val incorrectFormatRawBody = AnyContentAsJson(
    Json.parse(
      """
      |{
      |  "employerRef": true,
      |  "cessationDate": 400,
      |  "payrollId": [],
      |  "occupationalPension": 20
      |}
    """.stripMargin
    ))

  private val incorrectValueRawBody = AnyContentAsJson(
    Json.parse(
      s"""
       |{
       |  "employerRef": "notValid",
       |  "employerName": "${"a" * 75}",
       |  "startDate": "notValid",
       |  "cessationDate": "notValid",
       |  "payrollId": "${"b" * 75}",
       |  "occupationalPension": false
       |}
    """.stripMargin
    ))

  class Test extends MockCurrentDateTime with MockAppConfig {

    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter       = DateTimeFormat.forPattern("yyyy-MM-dd")

    MockCurrentDateTime.getDateTime
      .returns(DateTime.parse("2022-07-11", dateTimeFormatter))
      .anyNumberOfTimes()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new AddCustomEmploymentValidator()
  }

  "AddCustomEmploymentValidator" when {
    "running a validation" should {
      "return no errors for a valid request" in new Test {
        validator.validate(AddCustomEmploymentRawData(validNino, validTaxYear, validRawBody)) shouldBe Nil
      }

      "return RuleCessationDateBeforeTaxYearStart error when config when cessationDate is too early" in new Test {
        validator.validate(AddCustomEmploymentRawData(validNino, validTaxYear, requestJsonWithDates(cessationDate = "2020-04-01"))) shouldBe List(
          RuleCessationDateBeforeTaxYearStartError)
      }

      // parameter format error scenarios
      "return NinoFormatError error when the supplied NINO is invalid" in new Test {
        validator.validate(AddCustomEmploymentRawData("A12344A", validTaxYear, validRawBody)) shouldBe
          List(NinoFormatError)
      }

      "return TaxYearFormatError error for an invalid tax year format" in new Test {
        validator.validate(AddCustomEmploymentRawData(validNino, "20178", validRawBody)) shouldBe
          List(TaxYearFormatError)
      }

      "return RuleTaxYearRangeInvalidError error for an invalid tax year range" in new Test {
        validator.validate(AddCustomEmploymentRawData(validNino, "2018-20", validRawBody)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }

      "return multiple errors for multiple invalid request parameters" in new Test {
        validator.validate(AddCustomEmploymentRawData("notValid", "2018-20", validRawBody)) shouldBe
          List(NinoFormatError, RuleTaxYearRangeInvalidError)
      }

      // parameter rule error scenarios
      "return RuleTaxYearNotSupportedError error for an unsupported tax year" in new Test {
        validator.validate(AddCustomEmploymentRawData(validNino, "2019-20", validRawBody)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }

      "return RuleTaxYearNotEndedError error for a tax year which hasn't ended" in new Test {
        validator.validate(AddCustomEmploymentRawData(validNino, "2022-23", validRawBody)) shouldBe
          List(RuleTaxYearNotEndedError)
      }

      "not return RuleTaxYearNotEndedError error for a tax year which hasn't ended but temporal validation is disabled" in new Test {
        validator.validate(
          AddCustomEmploymentRawData(
            validNino,
            "2022-23",
            requestJsonWithDates(cessationDate = "2022-05-01"),
            temporalValidationEnabled = false)) shouldBe Nil
      }

      // body format error scenarios
      "return RuleIncorrectOrEmptyBodyError error for an empty request body" in new Test {
        validator.validate(AddCustomEmploymentRawData(validNino, validTaxYear, emptyRawBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "return RuleIncorrectOrEmptyBodyError error for an incorrect request body" in new Test {
        val paths: Seq[String] = List("/cessationDate", "/employerName", "/employerRef", "/occupationalPension", "/payrollId", "/startDate")

        validator.validate(AddCustomEmploymentRawData(validNino, validTaxYear, incorrectFormatRawBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(paths)))
      }

      // body value error scenarios
      "return multiple errors for incorrect field formats" in new Test {
        validator.validate(AddCustomEmploymentRawData(validNino, validTaxYear, incorrectValueRawBody)) shouldBe
          List(EmployerRefFormatError, EmployerNameFormatError, StartDateFormatError, CessationDateFormatError, PayrollIdFormatError)
      }

      "return multiple errors for dates which precede the current tax year and are incorrectly ordered" in new Test {
        validator.validate(
          AddCustomEmploymentRawData(validNino, validTaxYear, requestJsonWithDates(cessationDate = "2018-06-01"))
        ) shouldBe List(RuleCessationDateBeforeTaxYearStartError, RuleCessationDateBeforeStartDateError)
      }

      "return multiple errors for dates which exceed the current tax year and are incorrectly ordered" in new Test {
        validator.validate(
          AddCustomEmploymentRawData(validNino, validTaxYear, requestJsonWithDates(startDate = "2023-01-01", cessationDate = "2022-06-01"))
        ) shouldBe List(RuleStartDateAfterTaxYearEndError, RuleCessationDateBeforeStartDateError)
      }

      "return multiple errors for dates which are outside of allowed range" in new Test {
        validator.validate(
          AddCustomEmploymentRawData(validNino, validTaxYear, requestJsonWithDates(startDate = "0010-01-01", cessationDate = "2122-06-01"))
        ) shouldBe List(StartDateFormatError, CessationDateFormatError)
      }

    }
  }

}
