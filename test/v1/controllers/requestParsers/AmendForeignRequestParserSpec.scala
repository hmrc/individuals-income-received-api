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
import v1.mocks.validators.MockAmendForeignValidator
import v1.models.errors._
import v1.models.request.amendForeign._

class AmendForeignRequestParserSpec extends UnitSpec {

  val nino: String = "AA123456B"
  val taxYear: String = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |     "customerReference":"FOREIGNINCME123A",
      |     "earningsNotTaxableUK":"99999999999.99"
      |   },
      |   "unremittableForeignIncome": [
      |     {
      |       "countryCode":"FRA",
      |       "amountInForeignCurrency":"99.99",
      |       "amountTaxPaid":"1.0"
      |     }
      |   ]
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)

  private val fullForeignEarningsModel = ForeignEarnings(
    customerReference = Some("FOREIGNINCME123A"),
    earningsNotTaxableUK = 99999999999.99
  )

  private val fullUnremittableForeignIncomeModel = UnremittableForeignIncomeItem(
    countryCode = "FRA",
    amountInForeignCurrency = 99.99,
    amountTaxPaid = Some(1.0)
  )

  private val validRequestBodyModel = AmendForeignRequestBody(
    foreignEarnings = Some(fullForeignEarningsModel),
    unremittableForeignIncome = Some(Seq(fullUnremittableForeignIncomeModel))
  )

  private val amendForeignRawData = AmendForeignRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockAmendForeignValidator {
    lazy val parser: AmendForeignRequestParser = new AmendForeignRequestParser(
      validator = mockAmendForeignValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendForeignValidator.validate(amendForeignRawData).returns(Nil)

        parser.parseRequest(amendForeignRawData) shouldBe
          Right(AmendForeignRequest(Nino(nino), taxYear, validRequestBodyModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendForeignValidator.validate(amendForeignRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(amendForeignRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockAmendForeignValidator.validate(amendForeignRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(amendForeignRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple field value validation errors occur" in new Test {

        private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "foreignEarnings": {
            |     "customerReference":"This customer ref string is 91 characters long ------------------------------------------91",
            |     "earningsNotTaxableUK":"999999994444999.99"
            |   },
            |   "unremittableForeignIncome": [
            |     {
            |       "countryCode":"FRDA",
            |       "amountInForeignCurrency":"99.999999",
            |       "amountTaxPaid":"1.99999990"
            |     }
            |   ]
            |}
          """.stripMargin
        )

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

        private val allInvalidValueErrors = List(
          CountryCodeRuleError.copy(
            paths = Some(Seq("/unremittableForeignIncome/1/countryCode"))
          ),
          ValueFormatError.copy(
            paths = Some(List(
              "/foreignEarnings/earningsNotTaxableUK",
              "/unremittableForeignIncome/0/amountInForeignCurrency",
              "/unremittableForeignIncome/0/amountTaxPaid",
              "/unremittableForeignIncome/1/amountInForeignCurrency",
              "/unremittableForeignIncome/1/amountTaxPaid"
            )),
            message = "The field should be between 0 and 99999999999.99"
          ),
          CustomerRefFormatError.copy(
            paths = Some(List("/foreignEarnings/customerReference"))
          ),
          CountryCodeFormatError.copy(
            paths = Some(Seq("/unremittableForeignIncome/0/countryCode"))
          )
        )

        MockAmendForeignValidator.validate(amendForeignRawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(amendForeignRawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }
}