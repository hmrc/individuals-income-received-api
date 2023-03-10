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
import v1.mocks.validators.MockOtherEmploymentIncomeValidator
import v1.models.request.otherEmploymentIncome.{OtherEmploymentIncomeRequest, OtherEmploymentIncomeRequestRawData}

class OtherEmploymentIncomeRequestParserSpec extends UnitSpec {
  val nino: String                   = "AA123456B"
  val taxYear: TaxYear               = TaxYear.fromMtd("2019-20")
  val taxYearString: String          = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val rawData = OtherEmploymentIncomeRequestRawData(
    nino = nino,
    taxYear = taxYearString
  )

  trait Test extends MockOtherEmploymentIncomeValidator {

    lazy val parser: OtherEmploymentIncomeRequestParser = new OtherEmploymentIncomeRequestParser(
      validator = mockValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockOtherEmploymentIncomeValidator
          .validate(rawData)
          .returns(Nil)

        val result: Either[ErrorWrapper, OtherEmploymentIncomeRequest] =
          parser.parseRequest(rawData)
        result shouldBe Right(OtherEmploymentIncomeRequest(Nino(nino), taxYear))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation occurs" in new Test {
        MockOtherEmploymentIncomeValidator
          .validate(rawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        val result: Either[ErrorWrapper, OtherEmploymentIncomeRequest] =
          parser.parseRequest(rawData.copy(nino = "notANino"))
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))

      }

      "multiple validation errors occur" in new Test {
        MockOtherEmploymentIncomeValidator
          .validate(rawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        val result: Either[ErrorWrapper, OtherEmploymentIncomeRequest] =
          parser.parseRequest(rawData.copy(nino = "notANino", taxYear = "notATaxYear"))
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
