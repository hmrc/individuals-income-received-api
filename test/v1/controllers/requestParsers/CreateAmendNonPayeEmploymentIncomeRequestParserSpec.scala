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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.mocks.validators.MockCreateAmendNonPayeEmploymentIncomeValidator
import v1.models.domain.Nino
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v1.models.request.createAmendNonPayeEmploymentIncome._

class CreateAmendNonPayeEmploymentIncomeRequestParserSpec extends UnitSpec {
  val nino: String = "AA123456B"
  val taxYear: String = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "tips": 100.23
      |}
      |""".stripMargin
  )

  private val rawRequestBody = AnyContentAsJson(requestBodyJson)

  private val requestBody: CreateAmendNonPayeEmploymentIncomeRequestBody =
    CreateAmendNonPayeEmploymentIncomeRequestBody(
      100.23
    )

  private val createAmendNonPayeEmploymentIncomeRawData = CreateAmendNonPayeEmploymentIncomeRawData(
    nino = nino,
    taxYear = taxYear,
    body = rawRequestBody
  )

  trait Test extends MockCreateAmendNonPayeEmploymentIncomeValidator {
    lazy val parser: CreateAmendNonPayeEmploymentIncomeRequestParser = new CreateAmendNonPayeEmploymentIncomeRequestParser(
      validator = mockCreateAmendNonPayeEmploymentIncomeValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateAmendNonPayeEmploymentIncomeValidator.validate(createAmendNonPayeEmploymentIncomeRawData).returns(Nil)

        parser.parseRequest(createAmendNonPayeEmploymentIncomeRawData) shouldBe
          Right(CreateAmendNonPayeEmploymentIncomeRequest(Nino(nino), taxYear, requestBody))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation occurs" in new Test {
        MockCreateAmendNonPayeEmploymentIncomeValidator.validate(
          createAmendNonPayeEmploymentIncomeRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(createAmendNonPayeEmploymentIncomeRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))

      }

      "multiple validation errors occur" in new Test {
        MockCreateAmendNonPayeEmploymentIncomeValidator.validate(
          createAmendNonPayeEmploymentIncomeRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(createAmendNonPayeEmploymentIncomeRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }
}
