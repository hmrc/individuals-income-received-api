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
import v1.mocks.validators.MockRetrieveUkSavingsAccountValidator
import v1.models.request.retrieveUkSavingsAnnualSummary.{RetrieveUkSavingsAnnualSummaryRawData, RetrieveUkSavingsAnnualSummaryRequest}

class RetrieveUkSavingsAccountRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2021-22"
  val savingsAccountId: String       = "SAVKB2UVwUTBQGJ"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val retrieveUkSavingsAnnualSummaryRawData: RetrieveUkSavingsAnnualSummaryRawData = RetrieveUkSavingsAnnualSummaryRawData(
    nino = nino,
    taxYear = taxYear,
    savingsAccountId = savingsAccountId
  )

  trait Test extends MockRetrieveUkSavingsAccountValidator {

    lazy val parser: RetrieveUkSavingsAccountRequestParser = new RetrieveUkSavingsAccountRequestParser(
      validator = mockRetrieveUkSavingsAccountValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveUkSavingsAccountValidator.validate(retrieveUkSavingsAnnualSummaryRawData).returns(Nil)

        parser.parseRequest(retrieveUkSavingsAnnualSummaryRawData) shouldBe
          Right(RetrieveUkSavingsAnnualSummaryRequest(Nino(nino), TaxYear.fromMtd(taxYear), savingsAccountId))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveUkSavingsAccountValidator
          .validate(retrieveUkSavingsAnnualSummaryRawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(retrieveUkSavingsAnnualSummaryRawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur (NinoFormatError and TaxYearFormatError errors)" in new Test {
        MockRetrieveUkSavingsAccountValidator
          .validate(retrieveUkSavingsAnnualSummaryRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(retrieveUkSavingsAnnualSummaryRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple validation errors occur (NinoFormatError, TaxYearFormatError and SavingsAccountIdFormatError errors)" in new Test {
        MockRetrieveUkSavingsAccountValidator
          .validate(retrieveUkSavingsAnnualSummaryRawData)
          .returns(List(NinoFormatError, TaxYearFormatError, SavingsAccountIdFormatError))

        parser.parseRequest(retrieveUkSavingsAnnualSummaryRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError, SavingsAccountIdFormatError))))
      }
    }
  }

}
