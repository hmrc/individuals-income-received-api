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

package v1.controllers.requestParsers

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import support.UnitSpec
import v1.mocks.validators.MockIgnoreEmploymentValidator
import v1.models.request.ignoreEmployment.{IgnoreEmploymentRawData, IgnoreEmploymentRequest}

class IgnoreEmploymentRequestParserSpec extends UnitSpec {

  private val nino: String           = "AA123456B"
  private val taxYear: String        = "2021-22"
  private val employmentId           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val ignoreCustomEmploymentRawData = IgnoreEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId
  )

  private val ignoreEmploymentRequest = IgnoreEmploymentRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = employmentId
  )

  trait Test extends MockIgnoreEmploymentValidator {

    lazy val parser: IgnoreEmploymentRequestParser = new IgnoreEmploymentRequestParser(
      validator = mockIgnoreEmploymentValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockIgnoreEmploymentValidator.validate(ignoreCustomEmploymentRawData).returns(Nil)
        parser.parseRequest(ignoreCustomEmploymentRawData) shouldBe Right(ignoreEmploymentRequest)
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockIgnoreEmploymentValidator
          .validate(ignoreCustomEmploymentRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(ignoreCustomEmploymentRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockIgnoreEmploymentValidator
          .validate(ignoreCustomEmploymentRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(ignoreCustomEmploymentRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
