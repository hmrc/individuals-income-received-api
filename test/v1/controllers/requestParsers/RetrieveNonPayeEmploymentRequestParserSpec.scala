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
import v1.mocks.validators.MockRetrieveNonPayeEmploymentValidator
import v1.models.request.retrieveNonPayeEmploymentIncome.{RetrieveNonPayeEmploymentIncomeRawData, RetrieveNonPayeEmploymentIncomeRequest}

class RetrieveNonPayeEmploymentRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: TaxYear               = TaxYear.fromMtd("2021-22")
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  def rawData(sourceString: Option[String] = None): RetrieveNonPayeEmploymentIncomeRawData = RetrieveNonPayeEmploymentIncomeRawData(
    nino = nino,
    taxYear = taxYear.asMtd,
    source = sourceString
  )

  trait Test extends MockRetrieveNonPayeEmploymentValidator {

    lazy val parser: RetrieveNonPayeEmploymentRequestParser = new RetrieveNonPayeEmploymentRequestParser(
      validator = mockRetrieveNonPayeEmploymentValidator
    )

  }

  "parse" should {
    "return a request object" when {
      def parseSuccessfullyWith(sourceString: String, expectedRequest: RetrieveNonPayeEmploymentIncomeRequest): Unit =
        s"valid request data is supplied with source $sourceString" in new Test {
          MockRetrieveNonPayeEmploymentValidator.validate(rawData(Some(sourceString))).returns(Nil)

          parser.parseRequest(rawData(Some(sourceString))) shouldBe Right(expectedRequest)
        }

      val input = Seq(
        ("hmrcHeld", RetrieveNonPayeEmploymentIncomeRequest(Nino(nino), taxYear, MtdSourceEnum.hmrcHeld)),
        ("latest", RetrieveNonPayeEmploymentIncomeRequest(Nino(nino), taxYear, MtdSourceEnum.latest)),
        ("user", RetrieveNonPayeEmploymentIncomeRequest(Nino(nino), taxYear, MtdSourceEnum.user))
      )

      input.foreach(args => (parseSuccessfullyWith _).tupled(args))

      "default to 'latest' source" in new Test {
        MockRetrieveNonPayeEmploymentValidator.validate(rawData(None)).returns(Nil)

        parser.parseRequest(rawData(None)) shouldBe Right(RetrieveNonPayeEmploymentIncomeRequest(Nino(nino), taxYear, MtdSourceEnum.latest))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveNonPayeEmploymentValidator
          .validate(rawData())
          .returns(List(NinoFormatError))

        parser.parseRequest(rawData()) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockRetrieveNonPayeEmploymentValidator
          .validate(rawData())
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(rawData()) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
