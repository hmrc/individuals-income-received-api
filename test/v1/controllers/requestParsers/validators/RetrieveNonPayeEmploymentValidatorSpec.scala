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

import api.mocks.MockCurrentDateTime
import api.models.errors._
import config.AppConfig
import mocks.MockAppConfig
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import support.UnitSpec
import utils.CurrentDateTime
import v1.models.request.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeRawData

class RetrieveNonPayeEmploymentValidatorSpec extends UnitSpec {

  private val validNino               = "AA123456A"
  private val validTaxYear            = "2021-22"
  private val validSource             = "latest"
  private val minimumPermittedTaxYear = 2021

  class Test extends MockCurrentDateTime with MockAppConfig {

    implicit val dateTimeProvider: CurrentDateTime = mockCurrentDateTime
    val dateTimeFormatter: DateTimeFormatter       = DateTimeFormat.forPattern("yyyy-MM-dd")

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new RetrieveNonPayeEmploymentValidator()

    MockCurrentDateTime.getDateTime
      .returns(DateTime.parse("2022-07-11", dateTimeFormatter))
      .anyNumberOfTimes()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(minimumPermittedTaxYear)

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(RetrieveNonPayeEmploymentIncomeRawData(validNino, validTaxYear, Some(validSource))) shouldBe Nil
      }

      "a valid request is supplied without the employment source" in new Test {
        validator.validate(RetrieveNonPayeEmploymentIncomeRawData(validNino, validTaxYear, None)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(RetrieveNonPayeEmploymentIncomeRawData("A12344A", validTaxYear, Some(validSource))) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(RetrieveNonPayeEmploymentIncomeRawData(validNino, "20178", Some(validSource))) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "a tax year that is not supported is supplied" in new Test {
        validator.validate(RetrieveNonPayeEmploymentIncomeRawData(validNino, "2018-19", Some(validSource))) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an out of range tax year is supplied" in new Test {
        validator.validate(RetrieveNonPayeEmploymentIncomeRawData(validNino, "2020-22", Some(validSource))) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return NinoFormatError and TaxYearFormatError errors" when {
      "request supplied has invalid nino and tax year" in new Test {
        validator.validate(RetrieveNonPayeEmploymentIncomeRawData("A12344A", "20178", Some(validSource))) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }

    "return NinoFormatError, TaxYearFormatError and SourceFormatError errors" when {
      "request supplied has invalid nino, tax year and source" in new Test {
        validator.validate(RetrieveNonPayeEmploymentIncomeRawData("A12344A", "20178", Some("invalidSource"))) shouldBe
          List(NinoFormatError, TaxYearFormatError, SourceFormatError)
      }
    }
  }

}
