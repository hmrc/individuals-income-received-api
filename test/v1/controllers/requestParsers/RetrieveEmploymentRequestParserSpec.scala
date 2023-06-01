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

import api.models.domain.Nino
import api.models.errors._
import support.UnitSpec
import v1.mocks.validators.MockRetrieveEmploymentValidator
import v1.models.request.retrieveEmployment.{RetrieveEmploymentRawData, RetrieveEmploymentRequest}

class RetrieveEmploymentRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2021-22"
  val employmentId: String           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val retrieveCustomEmploymentRawData: RetrieveEmploymentRawData = RetrieveEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId
  )

  trait Test extends MockRetrieveEmploymentValidator {

    lazy val parser: RetrieveEmploymentRequestParser = new RetrieveEmploymentRequestParser(
      validator = mockRetrieveCustomEmploymentValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveCustomEmploymentValidator.validate(retrieveCustomEmploymentRawData).returns(Nil)

        parser.parseRequest(retrieveCustomEmploymentRawData) shouldBe
          Right(RetrieveEmploymentRequest(Nino(nino), taxYear, employmentId))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveCustomEmploymentValidator
          .validate(retrieveCustomEmploymentRawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(retrieveCustomEmploymentRawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur (NinoFormatError and TaxYearFormatError errors)" in new Test {
        MockRetrieveCustomEmploymentValidator
          .validate(retrieveCustomEmploymentRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(retrieveCustomEmploymentRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple validation errors occur (NinoFormatError, TaxYearFormatError and EmploymentIdFormatError errors)" in new Test {
        MockRetrieveCustomEmploymentValidator
          .validate(retrieveCustomEmploymentRawData)
          .returns(List(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError))

        parser.parseRequest(retrieveCustomEmploymentRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError))))
      }
    }
  }

}
