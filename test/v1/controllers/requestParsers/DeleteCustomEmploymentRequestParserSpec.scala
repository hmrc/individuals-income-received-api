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
import v1.mocks.validators.MockDeleteCustomEmploymentValidator
import v1.models.request.deleteCustomEmployment.{DeleteCustomEmploymentRawData, DeleteCustomEmploymentRequest}

class DeleteCustomEmploymentRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2021-22"
  val employmentId: String           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val deleteCustomEmploymentRawData: DeleteCustomEmploymentRawData = DeleteCustomEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId
  )

  trait Test extends MockDeleteCustomEmploymentValidator {

    lazy val parser: DeleteCustomEmploymentRequestParser = new DeleteCustomEmploymentRequestParser(
      validator = mockDeleteCustomEmploymentValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockDeleteCustomEmploymentValidator.validate(deleteCustomEmploymentRawData).returns(Nil)

        parser.parseRequest(deleteCustomEmploymentRawData) shouldBe
          Right(DeleteCustomEmploymentRequest(Nino(nino), taxYear, employmentId))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockDeleteCustomEmploymentValidator
          .validate(deleteCustomEmploymentRawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(deleteCustomEmploymentRawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur (NinoFormatError and TaxYearFormatError errors)" in new Test {
        MockDeleteCustomEmploymentValidator
          .validate(deleteCustomEmploymentRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(deleteCustomEmploymentRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple validation errors occur (NinoFormatError, TaxYearFormatError and EmploymentIdFormatError errors)" in new Test {
        MockDeleteCustomEmploymentValidator
          .validate(deleteCustomEmploymentRawData)
          .returns(List(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError))

        parser.parseRequest(deleteCustomEmploymentRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError))))
      }
    }
  }

}
