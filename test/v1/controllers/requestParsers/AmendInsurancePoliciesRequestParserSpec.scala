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
import v1.mocks.validators.MockAmendInsurancePoliciesValidator
import v1.models.request.amendInsurancePolicies._

class AmendInsurancePoliciesRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear                        = TaxYear.fromMtd("2020-21")
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "lifeAnnuity":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "voidedIsa":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       }
      |   ],
      |   "foreign":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)

  val rawData: AmendInsurancePoliciesRawData = AmendInsurancePoliciesRawData(
    nino = nino,
    taxYear = taxYear.asMtd,
    body = validRawRequestBody
  )

  val lifeInsurance: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val capitalRedemption: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val lifeAnnuity: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val voidedIsa: Seq[AmendVoidedIsaPoliciesItem] = Seq(
    AmendVoidedIsaPoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12)
    ),
    AmendVoidedIsaPoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12)
    )
  )

  val foreign: Seq[AmendForeignPoliciesItem] = Seq(
    AmendForeignPoliciesItem(
      customerReference = Some("INPOLY123A"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15)
    ),
    AmendForeignPoliciesItem(
      customerReference = Some("INPOLY123A"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15)
    )
  )

  val amendInsurancePoliciesRequestBody: AmendInsurancePoliciesRequestBody = AmendInsurancePoliciesRequestBody(
    lifeInsurance = Some(lifeInsurance),
    capitalRedemption = Some(capitalRedemption),
    lifeAnnuity = Some(lifeAnnuity),
    voidedIsa = Some(voidedIsa),
    foreign = Some(foreign)
  )

  val requestData: AmendInsurancePoliciesRequest = AmendInsurancePoliciesRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = amendInsurancePoliciesRequestBody
  )

  trait Test extends MockAmendInsurancePoliciesValidator {

    lazy val parser: AmendInsurancePoliciesRequestParser = new AmendInsurancePoliciesRequestParser(
      validator = mockAmendInsurancePoliciesValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendInsurancePoliciesValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe Right(requestData)
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendInsurancePoliciesValidator
          .validate(rawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(rawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockAmendInsurancePoliciesValidator
          .validate(rawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(rawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple field value validation errors occur" in new Test {

        private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "lifeInsurance":[
            |       {
            |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |           "event": "Death of spouse",
            |           "gainAmount": 2000.999,
            |           "taxPaid": true,
            |           "yearsHeld": -15,
            |           "yearsHeldSinceLastGain": 12,
            |           "deficiencyRelief": 5000.999
            |       },
            |       {
            |           "customerReference": "INPOLY123A",
            |           "event": "This event string is 91 characters long ------------------------------------------------ 91",
            |           "gainAmount": 2000.99,
            |           "taxPaid": true,
            |           "yearsHeld": 15,
            |           "yearsHeldSinceLastGain": 12,
            |           "deficiencyRelief": 5000.99
            |       }
            |   ],
            |   "capitalRedemption":[
            |       {
            |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |           "event": "Death of spouse",
            |           "gainAmount": 3000.999,
            |           "taxPaid": true,
            |           "yearsHeld": -15,
            |           "yearsHeldSinceLastGain": 12,
            |           "deficiencyRelief": 5000.99
            |       },
            |       {
            |           "customerReference": "INPOLY123A",
            |           "event": "Death of spouse",
            |           "gainAmount": 2000.99,
            |           "taxPaid": true,
            |           "yearsHeld": 15,
            |           "yearsHeldSinceLastGain": 120,
            |           "deficiencyRelief": 5000.999
            |       }
            |   ],
            |   "lifeAnnuity":[
            |       {
            |           "customerReference": "INPOLY123A",
            |           "event": "Death of spouse",
            |           "gainAmount": 2000.99,
            |           "taxPaid": true,
            |           "yearsHeld": -15,
            |           "yearsHeldSinceLastGain": 12,
            |           "deficiencyRelief": 5000.999
            |       },
            |       {
            |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |           "event": "This event string is 91 characters long ------------------------------------------------ 91",
            |           "gainAmount": 5000.99,
            |           "taxPaid": true,
            |           "yearsHeld": 15,
            |           "yearsHeldSinceLastGain": 12,
            |           "deficiencyRelief": 5000.99
            |       }
            |   ],
            |   "voidedIsa":[
            |       {
            |           "customerReference": "INPOLY123A",
            |           "event": "Death of spouse",
            |           "gainAmount": 2000.99,
            |           "taxPaidAmount": 5000.99,
            |           "yearsHeld": -15,
            |           "yearsHeldSinceLastGain": 120
            |       },
            |       {
            |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |           "event": "Death of spouse",
            |           "gainAmount": 5000.999,
            |           "taxPaidAmount": 5000.999,
            |           "yearsHeld": 15,
            |           "yearsHeldSinceLastGain": 12
            |       }
            |   ],
            |   "foreign":[
            |       {
            |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |           "gainAmount": 5000.99,
            |           "taxPaidAmount": 5000.999,
            |           "yearsHeld": 15
            |       },
            |       {
            |           "customerReference": "INPOLY123A",
            |           "gainAmount": 2000.999,
            |           "taxPaidAmount": 5000.99,
            |           "yearsHeld": -15
            |       }
            |   ]
            |}
          """.stripMargin
        )

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

        private val allInvalidValueErrors = List(
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(
              List(
                "/lifeInsurance/0/gainAmount",
                "/lifeInsurance/0/deficiencyRelief",
                "/capitalRedemption/0/gainAmount",
                "/capitalRedemption/1/deficiencyRelief",
                "/lifeAnnuity/0/deficiencyRelief",
                "/voidedIsa/1/gainAmount",
                "/voidedIsa/1/taxPaidAmount",
                "/foreign/0/taxPaidAmount",
                "/foreign/1/gainAmount"
              ))
          ),
          EventFormatError.copy(
            paths = Some(
              List(
                "/lifeInsurance/1/event",
                "/lifeAnnuity/1/event"
              ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99",
            paths = Some(
              List(
                "/lifeInsurance/0/yearsHeld",
                "/capitalRedemption/0/yearsHeld",
                "/capitalRedemption/1/yearsHeldSinceLastGain",
                "/lifeAnnuity/0/yearsHeld",
                "/voidedIsa/0/yearsHeld",
                "/voidedIsa/0/yearsHeldSinceLastGain",
                "/foreign/1/yearsHeld"
              ))
          ),
          CustomerRefFormatError.copy(
            paths = Some(
              List(
                "/lifeInsurance/0/customerReference",
                "/capitalRedemption/0/customerReference",
                "/lifeAnnuity/1/customerReference",
                "/voidedIsa/1/customerReference",
                "/foreign/0/customerReference"
              ))
          )
        )

        MockAmendInsurancePoliciesValidator
          .validate(rawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(rawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }

}
