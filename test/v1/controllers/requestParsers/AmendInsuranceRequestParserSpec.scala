/*
 * Copyright 2020 HM Revenue & Customs
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
import v1.mocks.validators.MockAmendInsuranceValidator
import v1.models.domain.DesTaxYear
import v1.models.errors._
import v1.models.request.insurancePolicies.amend.{AmendRawData, AmendRequest}
import v1.models.request.savings.amend._

class AmendInsuranceRequestParserSpec extends UnitSpec{

  val nino: String = "AA123456B"  //Needs formatting
  val taxYear: String = "2017-18"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |      {
      |         "customerReference":"INPOLY123A",
      |         "event":"Death of spouse",
      |         "gainAmount":1.23,
      |         "taxPaid":1.23,
      |         "yearsHeld":2,
      |         "yearsHeldSinceLastGain":1,
      |         "deficiencyRelief":1.23
      |      }
      |   ],
      |   "capitalRedemption":[
      |      {
      |         "customerReference":"INPOLY123A",
      |         "event":"Death of spouse",
      |         "gainAmount":1.23,
      |         "taxPaid":1.23,
      |         "yearsHeld":2,
      |         "yearsHeldSinceLastGain":1,
      |         "deficiencyRelief":1.23
      |      }
      |   ],
      |   "lifeAnnuity":[
      |      {
      |         "customerReference":"INPOLY123A",
      |         "event":"Death of spouse",
      |         "gainAmount":1.23,
      |         "taxPaid":1.23,
      |         "yearsHeld":2,
      |         "yearsHeldSinceLastGain":1,
      |         "deficiencyRelief":1.23
      |      }
      |   ],
      |   "voidedIsa":[
      |      {
      |         "customerReference":"INPOLY123A",
      |         "event":"Death of spouse",
      |         "gainAmount":1.23,
      |         "taxPaid":1.23,
      |         "yearsHeld":2,
      |         "yearsHeldSinceLastGain":1
      |      }
      |   ],
      |   "foreign":[
      |      {
      |         "customerReference":"INPOLY123A",
      |         "gainAmount":1.23,
      |         "taxPaid":1.23,
      |         "yearsHeld":2
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)

  private val validRequestBodyModel = AmendSavingsRequestBody()  //Needs change
//    securities = Some(AmendSecurities(
//      taxTakenOff = Some(100.11),
//      grossAmount = Some(100.12),
//      netAmount = Some(100.13)
//    )),
//    foreignInterest = Some(Seq(
//      AmendForeignInterest(
//        amountBeforeTax = Some(200.11),
//        countryCode = "GBR",
//        taxTakenOff = Some(200.12),
//        specialWithholdingTax = Some(200.13),
//        taxableAmount = 200.14,
//        foreignTaxCreditRelief = false
//      ),
//      AmendForeignInterest(
//        amountBeforeTax = Some(300.11),
//        countryCode = "GBR",
//        taxTakenOff = Some(300.12),
//        specialWithholdingTax = Some(300.13),
//        taxableAmount = 300.14,
//        foreignTaxCreditRelief = true
//      )
//    ))


  private val amendRawData = AmendRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockAmendInsuranceValidator {
    lazy val parser: AmendInsuranceRequestParser = new AmendInsuranceRequestParser(
      validator = mockAmendInsuranceValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendInsuranceValidator.validate(amendRawData).returns(Nil)

        parser.parseRequest(amendRawData) shouldBe
          Right(AmendRequest(Nino(nino), DesTaxYear.fromMtd(taxYear), validRequestBodyModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendInsuranceValidator.validate(amendRawData.copy(nino = "notANino"))   // Needs changing
          .returns(List(NinoFormatError))

        parser.parseRequest(amendRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(None, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockAmendInsuranceValidator.validate(amendRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(amendRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple field value validation errors occur" in new Test {

        private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |
            |
            |
            |
          """.stripMargin
        )

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

        private val allInvalidValueErrors = List(
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(List(
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
            paths = Some(List(
              "/foreignInterest/0/countryCode",
              "/foreignInterest/1/countryCode"
            ))
          )
        )

        MockAmendInsuranceValidator.validate(amendRawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(amendRawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }
}
