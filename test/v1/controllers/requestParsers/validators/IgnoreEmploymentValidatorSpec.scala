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
import support.UnitSpec
import utils.CurrentDateTime
import v1.models.request.ignoreEmployment.IgnoreEmploymentRawData

class IgnoreEmploymentValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino         = "AA123456A"
  private val validTaxYear      = "2021-22"
  private val validEmploymentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  class Test extends MockCurrentDateTime with MockAppConfig {

    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter       = DateTimeFormat.forPattern("yyyy-MM-dd")

    MockCurrentDateTime.getDateTime
      .returns(DateTime.parse("2022-07-11", dateTimeFormatter))
      .anyNumberOfTimes()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new IgnoreEmploymentValidator()
  }

  "IgnoreEmploymentValidator" when {
    "running a validation" should {
      "return no errors for a valid request" in new Test {
        validator.validate(IgnoreEmploymentRawData(validNino, validTaxYear, validEmploymentId)) shouldBe Nil
      }

      "return NinoFormatError error when the supplied NINO is invalid" in new Test {
        validator.validate(IgnoreEmploymentRawData("A12344A", validTaxYear, validEmploymentId)) shouldBe
          List(NinoFormatError)
      }

      "return TaxYearFormatError error for an invalid tax year format" in new Test {
        validator.validate(IgnoreEmploymentRawData(validNino, "20178", validEmploymentId)) shouldBe
          List(TaxYearFormatError)
      }

      "return RuleTaxYearRangeInvalidError error for an invalid tax year range" in new Test {
        validator.validate(IgnoreEmploymentRawData(validNino, "2018-20", validEmploymentId)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }

      "return EmploymentIdFormatError error for an invalid employment id" in new Test {
        validator.validate(IgnoreEmploymentRawData(validNino, validTaxYear, "notValid")) shouldBe
          List(EmploymentIdFormatError)
      }

      "return multiple errors for multiple invalid request parameters" in new Test {
        validator.validate(IgnoreEmploymentRawData("notValid", "2018-20", "invalid")) shouldBe
          List(NinoFormatError, RuleTaxYearRangeInvalidError, EmploymentIdFormatError)
      }

      "return RuleTaxYearNotSupportedError error for an unsupported tax year" in new Test {
        validator.validate(IgnoreEmploymentRawData(validNino, "2019-20", validEmploymentId)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }
  }

}
