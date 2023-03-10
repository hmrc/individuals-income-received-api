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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.mocks.validators.MockCreateAmendUkDividendsAnnualSummaryValidator
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary._

class CreateAmendUkDividendsIncomeAnnualSummaryRequestParserSpec extends UnitSpec {
  val nino: String                      = "AA123456B"
  val taxYear: TaxYear                  = TaxYear.fromMtd("2019-20")
  val taxYearString: String             = "2019-20"
  implicit val correlationId: String    = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  val validUkDividends: BigDecimal      = 55844806400.99
  val validOtherUkDividends: BigDecimal = 60267421355.99

  private val requestBodyJson: JsValue = Json.parse(s"""
                                                       |{
                                                       | "ukDividends": $validUkDividends,
                                                       | "otherUkDividends": $validOtherUkDividends
                                                       |}
                                                       |""".stripMargin)

  private val rawRequestBody = AnyContentAsJson(requestBodyJson)

  private val requestBody: CreateAmendUkDividendsIncomeAnnualSummaryBody =
    CreateAmendUkDividendsIncomeAnnualSummaryBody(
      Some(validUkDividends),
      Some(validOtherUkDividends)
    )

  private val rawData = CreateAmendUkDividendsIncomeAnnualSummaryRawData(
    nino = nino,
    taxYear = taxYearString,
    body = rawRequestBody
  )

  trait Test extends MockCreateAmendUkDividendsAnnualSummaryValidator {

    lazy val parser: CreateAmendUkDividendsIncomeAnnualSummaryRequestParser = new CreateAmendUkDividendsIncomeAnnualSummaryRequestParser(
      validator = mockValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateAmendUkDividendsIncomeAnnualSummaryValidator
          .validate(rawData)
          .returns(Nil)

        val result: Either[ErrorWrapper, CreateAmendUkDividendsIncomeAnnualSummaryRequest] =
          parser.parseRequest(rawData)
        result shouldBe Right(CreateAmendUkDividendsIncomeAnnualSummaryRequest(Nino(nino), taxYear, requestBody))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation occurs" in new Test {
        MockCreateAmendUkDividendsIncomeAnnualSummaryValidator
          .validate(rawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        val result: Either[ErrorWrapper, CreateAmendUkDividendsIncomeAnnualSummaryRequest] =
          parser.parseRequest(rawData.copy(nino = "notANino"))
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))

      }

      "multiple validation errors occur" in new Test {
        MockCreateAmendUkDividendsIncomeAnnualSummaryValidator
          .validate(rawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        val result: Either[ErrorWrapper, CreateAmendUkDividendsIncomeAnnualSummaryRequest] =
          parser.parseRequest(rawData.copy(nino = "notANino", taxYear = "notATaxYear"))
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
