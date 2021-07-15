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

package v1.controllers.requestParsers

import support.UnitSpec
import v1.mocks.validators.MockRetrieveAllCgtValidator
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.request.retrieveAllCgt.{RetrieveAllCgtRawData, RetrieveAllCgtRequest}

class RetrieveAllCgtRequestParserSpec extends UnitSpec {

  val nino: String = "AA123456B"
  val taxYear: String = "2021-22"
  val source: String = "hmrcHeld"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val retrieveAllCgtRawData: RetrieveAllCgtRawData = RetrieveAllCgtRawData(
    nino = nino,
    taxYear = taxYear,
    source = source
  )

  trait Test extends MockRetrieveAllCgtValidator {
    lazy val parser: RetrieveAllCgtRequestParser = new RetrieveAllCgtRequestParser(
      validator = mockRetrieveAllCgtValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveAllCgtValidator.validate(retrieveAllCgtRawData).returns(Nil)

        parser.parseRequest(retrieveAllCgtRawData) shouldBe
          Right(RetrieveAllCgtRequest(Nino(nino), taxYear, source))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveAllCgtValidator.validate(retrieveAllCgtRawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(retrieveAllCgtRawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur (NinoFormatError and TaxYearFormatError errors)" in new Test {
        MockRetrieveAllCgtValidator.validate(retrieveAllCgtRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(retrieveAllCgtRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple validation errors occur (NinoFormatError, TaxYearFormatError and SourceFormatError errors)" in new Test {
        MockRetrieveAllCgtValidator.validate(retrieveAllCgtRawData)
          .returns(List(NinoFormatError, TaxYearFormatError, SourceFormatError))

        parser.parseRequest(retrieveAllCgtRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError))))
      }
    }
  }
}