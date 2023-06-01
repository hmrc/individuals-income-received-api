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
import v1.mocks.validators.MockCreateCreateAmendSavingsValidator
import v1.models.request.amendSavings._

class CreateAmendSavingsRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "securities": {
      |      "taxTakenOff": 100.11,
      |      "grossAmount": 100.12,
      |      "netAmount": 100.13
      |   },
      |   "foreignInterest": [
      |       {
      |          "amountBeforeTax": 200.11,
      |          "countryCode": "GBR",
      |          "taxTakenOff": 200.12,
      |          "specialWithholdingTax": 200.13,
      |          "taxableAmount": 200.14,
      |          "foreignTaxCreditRelief": false
      |       },
      |       {
      |          "amountBeforeTax": 300.11,
      |          "countryCode": "GBR",
      |          "taxTakenOff": 300.12,
      |          "specialWithholdingTax": 300.13,
      |          "taxableAmount": 300.14,
      |          "foreignTaxCreditRelief": true
      |       }
      |    ]
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)

  private val validRequestBodyModel = CreateAmendSavingsRequestBody(
    securities = Some(
      AmendSecurities(
        taxTakenOff = Some(100.11),
        grossAmount = 100.12,
        netAmount = Some(100.13)
      )),
    foreignInterest = Some(
      Seq(
        AmendForeignInterestItem(
          amountBeforeTax = Some(200.11),
          countryCode = "GBR",
          taxTakenOff = Some(200.12),
          specialWithholdingTax = Some(200.13),
          taxableAmount = 200.14,
          foreignTaxCreditRelief = Some(false)
        ),
        AmendForeignInterestItem(
          amountBeforeTax = Some(300.11),
          countryCode = "GBR",
          taxTakenOff = Some(300.12),
          specialWithholdingTax = Some(300.13),
          taxableAmount = 300.14,
          foreignTaxCreditRelief = Some(true)
        )
      ))
  )

  private val amendSavingsRawData = CreateAmendSavingsRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockCreateCreateAmendSavingsValidator {

    lazy val parser: CreateAmendSavingsRequestParser = new CreateAmendSavingsRequestParser(
      validator = mockCreateAmendSavingsValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateAmendSavingsValidator.validate(amendSavingsRawData).returns(Nil)

        parser.parseRequest(amendSavingsRawData) shouldBe
          Right(CreateAmendSavingsRequest(Nino(nino), TaxYear.fromMtd(taxYear), validRequestBodyModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockCreateAmendSavingsValidator
          .validate(amendSavingsRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(amendSavingsRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockCreateAmendSavingsValidator
          .validate(amendSavingsRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(amendSavingsRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple field value validation errors occur" in new Test {

        private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "securities": {
            |      "taxTakenOff": 100.111,
            |      "grossAmount": -100.12,
            |      "netAmount": 999999999991.99
            |   },
            |   "foreignInterest": [
            |       {
            |          "amountBeforeTax": -200.11,
            |          "countryCode": "SkegVegas",
            |          "taxTakenOff": 200.121,
            |          "specialWithholdingTax": 999999999991.13,
            |          "taxableAmount": -200.14,
            |          "foreignTaxCreditRelief": false
            |       },
            |       {
            |          "amountBeforeTax": -300.11,
            |          "countryCode": "SunSeaAndSand",
            |          "taxTakenOff": -300.100,
            |          "specialWithholdingTax": -300.134,
            |          "taxableAmount": -300.14,
            |          "foreignTaxCreditRelief": true
            |       }
            |    ]
            |}
          """.stripMargin
        )

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

        private val allInvalidValueErrors = List(
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(
              List(
                "/securities/taxTakenOff",
                "/securities/grossAmount",
                "/securities/netAmount",
                "/foreignInterest/0/amountBeforeTax",
                "/foreignInterest/0/taxTakenOff",
                "/foreignInterest/0/specialWithholdingTax",
                "/foreignInterest/0/taxableAmount",
                "/foreignInterest/1/amountBeforeTax",
                "/foreignInterest/1/taxTakenOff",
                "/foreignInterest/1/specialWithholdingTax",
                "/foreignInterest/1/taxableAmount"
              ))
          ),
          CountryCodeFormatError.copy(
            paths = Some(
              List(
                "/foreignInterest/0/countryCode",
                "/foreignInterest/1/countryCode"
              ))
          )
        )

        MockCreateAmendSavingsValidator
          .validate(amendSavingsRawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(amendSavingsRawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }

}
