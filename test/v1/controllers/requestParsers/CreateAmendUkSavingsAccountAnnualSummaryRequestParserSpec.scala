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
import v1.mocks.validators.MockCreateAmendUkSavingsAccountAnnualSummaryValidator
import v1.models.request.createAmendUkSavingsAnnualSummary._

class CreateAmendUkSavingsAccountAnnualSummaryRequestParserSpec extends UnitSpec {
  val nino: String                   = "AA123456B"
  val taxYear: TaxYear               = TaxYear.fromMtd("2019-20")
  val taxYearString: String          = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  val validTaxedUkInterest: Double   = 31554452289.99
  val validUntaxedUkInterest: Double = 91523009816.00
  val validSavingsAccountId: String  = "SAVKB2UVwUTBQGJ"

  private val requestBodyJson: JsValue = Json.parse(s"""
       |{
       | "taxedUkInterest": $validTaxedUkInterest,
       | "untaxedUkInterest": $validUntaxedUkInterest
       |}
       |""".stripMargin)

  private val rawRequestBody = AnyContentAsJson(requestBodyJson)

  private val requestBody: CreateAmendUkSavingsAnnualSummaryBody =
    CreateAmendUkSavingsAnnualSummaryBody(
      Some(validTaxedUkInterest),
      Some(validUntaxedUkInterest)
    )

  private val rawData = CreateAmendUkSavingsAnnualSummaryRawData(
    nino = nino,
    taxYear = taxYearString,
    savingsAccountId = validSavingsAccountId,
    body = rawRequestBody
  )

  trait Test extends MockCreateAmendUkSavingsAccountAnnualSummaryValidator {

    lazy val parser: CreateAmendUkSavingsAccountAnnualSummaryRequestParser = new CreateAmendUkSavingsAccountAnnualSummaryRequestParser(
      validator = mockValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateAmendUkSavingsAccountAnnualSummaryValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe
          Right(CreateAmendUkSavingsAnnualSummaryRequest(Nino(nino), taxYear, validSavingsAccountId, requestBody))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation occurs" in new Test {
        MockCreateAmendUkSavingsAccountAnnualSummaryValidator
          .validate(rawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(rawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))

      }

      "multiple validation errors occur" in new Test {
        MockCreateAmendUkSavingsAccountAnnualSummaryValidator
          .validate(rawData.copy(nino = "notANino", taxYear = "notATaxYear", savingsAccountId = "notASavingsAccountId"))
          .returns(List(NinoFormatError, TaxYearFormatError, SavingsAccountIdFormatError))

        parser.parseRequest(rawData.copy(nino = "notANino", taxYear = "notATaxYear", savingsAccountId = "notASavingsAccountId")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError, SavingsAccountIdFormatError))))
      }
    }
  }

}
