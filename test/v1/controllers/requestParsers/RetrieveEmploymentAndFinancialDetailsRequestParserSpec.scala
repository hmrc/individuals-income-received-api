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

import api.models.domain.{MtdSourceEnum, Nino, TaxYear}
import api.models.errors._
import support.UnitSpec
import v1.mocks.validators.MockRetrieveFinancialDetailsValidator
import v1.models.request.retrieveFinancialDetails.{RetrieveEmploymentAndFinancialDetailsRequest, RetrieveFinancialDetailsRawData}

class RetrieveEmploymentAndFinancialDetailsRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2021-22"
  val employmentId: String           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val validSource: String            = "latest"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val retrieveFinancialDetailsRawData: RetrieveFinancialDetailsRawData = RetrieveFinancialDetailsRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId,
    source = Some(validSource)
  )

  trait Test extends MockRetrieveFinancialDetailsValidator {

    lazy val parser: RetrieveFinancialDetailsRequestParser = new RetrieveFinancialDetailsRequestParser(
      validator = mockRetrieveFinancialDetailsValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveFinancialDetailsValidator.validate(retrieveFinancialDetailsRawData).returns(Nil)

        parser.parseRequest(retrieveFinancialDetailsRawData) shouldBe
          Right(RetrieveEmploymentAndFinancialDetailsRequest(Nino(nino), TaxYear.fromMtd(taxYear), employmentId, MtdSourceEnum.latest))
      }

      "valid request data with out source is supplied" in new Test {
        MockRetrieveFinancialDetailsValidator.validate(retrieveFinancialDetailsRawData.copy(source = None)).returns(Nil)

        parser.parseRequest(retrieveFinancialDetailsRawData.copy(source = None)) shouldBe
          Right(RetrieveEmploymentAndFinancialDetailsRequest(Nino(nino), TaxYear.fromMtd(taxYear), employmentId, MtdSourceEnum.latest))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveFinancialDetailsValidator
          .validate(retrieveFinancialDetailsRawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(retrieveFinancialDetailsRawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur (NinoFormatError and TaxYearFormatError errors)" in new Test {
        MockRetrieveFinancialDetailsValidator
          .validate(retrieveFinancialDetailsRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(retrieveFinancialDetailsRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple validation errors occur (NinoFormatError, TaxYearFormatError, EmploymentIdFormatError and SourceFormatError errors)" in new Test {
        MockRetrieveFinancialDetailsValidator
          .validate(retrieveFinancialDetailsRawData)
          .returns(List(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError, SourceFormatError))

        parser.parseRequest(retrieveFinancialDetailsRawData) shouldBe
          Left(
            ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError, SourceFormatError))))
      }
    }
  }

}
