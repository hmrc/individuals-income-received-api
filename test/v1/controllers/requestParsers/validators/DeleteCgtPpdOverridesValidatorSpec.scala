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

import api.models.errors.{NinoFormatError, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import config.AppConfig
import mocks.MockAppConfig
import support.UnitSpec
import v1.models.request.deleteCgtPpdOverrides.DeleteCgtPpdOverridesRawData

class DeleteCgtPpdOverridesValidatorSpec extends UnitSpec {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2019-20"

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new DeleteCgtPpdOverridesValidator()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2020)
      .anyNumberOfTimes()

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(DeleteCgtPpdOverridesRawData(validNino, validTaxYear)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(DeleteCgtPpdOverridesRawData("A12344A", validTaxYear)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(DeleteCgtPpdOverridesRawData(validNino, "20178")) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in new Test {
        validator.validate(DeleteCgtPpdOverridesRawData(validNino, "2019-21")) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(DeleteCgtPpdOverridesRawData(validNino, "2016-17")) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validator.validate(DeleteCgtPpdOverridesRawData("A12344A", "20178")) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }

}
