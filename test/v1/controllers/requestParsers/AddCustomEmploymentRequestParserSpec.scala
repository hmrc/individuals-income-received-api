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
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockAddCustomEmploymentValidator
import v1.models.errors._
import v1.models.request.addCustomEmployment._

class AddCustomEmploymentRequestParserSpec extends UnitSpec {

  private val nino: String = "AA123456B"
  private val taxYear: String = "2017-18"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestJson: JsValue = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412"
      |}
    """.stripMargin
  )

  private val validRawBody = AnyContentAsJson(validRequestJson)

  private val addCustomEmploymentRawData = AddCustomEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawBody
  )

  private val addCustomEmploymentRequestBody = AddCustomEmploymentRequestBody(
    employerRef = Some("123/AZ12334"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412")
  )

  private val addCustomEmploymentRequest = AddCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = addCustomEmploymentRequestBody
  )

  trait Test extends MockAddCustomEmploymentValidator {
    lazy val parser: AddCustomEmploymentRequestParser = new AddCustomEmploymentRequestParser(
      validator = mockAddCustomEmploymentValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAddCustomEmploymentValidator.validate(addCustomEmploymentRawData).returns(Nil)
        parser.parseRequest(addCustomEmploymentRawData) shouldBe Right(addCustomEmploymentRequest)
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAddCustomEmploymentValidator.validate(addCustomEmploymentRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(addCustomEmploymentRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockAddCustomEmploymentValidator.validate(addCustomEmploymentRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(addCustomEmploymentRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple field value validation errors occur" in new Test {

        private val invalidValueRequestJson: JsValue = Json.parse(
          s"""
             |{
             |  "employerRef": "notValid",
             |  "employerName": "${"a" * 75}",
             |  "startDate": "notValid",
             |  "cessationDate": "notValid",
             |  "payrollId": "${"b" * 75}"
             |}
            """.stripMargin
        )

        private val invalidValueRawBody = AnyContentAsJson(invalidValueRequestJson)

        private val errors = List(
          EmployerRefFormatError,
          EmployerNameFormatError,
          StartDateFormatError,
          CessationDateFormatError,
          PayrollIdFormatError
        )

        MockAddCustomEmploymentValidator.validate(addCustomEmploymentRawData.copy(body = invalidValueRawBody))
          .returns(errors)

        parser.parseRequest(addCustomEmploymentRawData.copy(body = invalidValueRawBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(errors)))
      }
    }
  }
}