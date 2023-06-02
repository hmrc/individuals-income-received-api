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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.validations.ValueFormatErrorMessages
import api.models.errors._
import config.AppConfig
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.models.request.amendInsurancePolicies.AmendInsurancePoliciesRawData

class AmendInsurancePoliciesValidatorSpec extends UnitSpec with ValueFormatErrorMessages {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

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

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": "no",
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |   "foreign":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidEventRequestBodyJson: JsValue = Json.parse(
    s"""
       |{
       |   "lifeAnnuity":[
       |       {
       |           "customerReference": "INPOLY123A",
       |           "event": "${"a" * 91} ",
       |           "gainAmount": 2000.99,
       |           "taxPaid": true,
       |           "yearsHeld": 15,
       |           "yearsHeldSinceLastGain": 12,
       |           "deficiencyRelief": 5000.99
       |       }
       |   ]
       |}
    """.stripMargin
  )

  private val invalidLifeInsuranceRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": -2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCapitalRedemptionRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.999
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidLifeAnnuityRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeAnnuity":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.999,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidVoidedIsaRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "voidedIsa":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 300
      |       }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidForeignRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreign":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 150
      |       }
      |   ]
      |}
    """.stripMargin
  )

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

  private val validRawRequestBody                    = AnyContentAsJson(validRequestBodyJson)
  private val emptyRawRequestBody                    = AnyContentAsJson(emptyRequestBodyJson)
  private val nonsenseRawRequestBody                 = AnyContentAsJson(nonsenseRequestBodyJson)
  private val nonValidRawRequestBody                 = AnyContentAsJson(nonValidRequestBodyJson)
  private val missingMandatoryFieldRequestBody       = AnyContentAsJson(missingMandatoryFieldJson)
  private val invalidCustomerRefRawRequestBody       = AnyContentAsJson(invalidCustomerRefRequestBodyJson)
  private val invalidEventRawRequestBody             = AnyContentAsJson(invalidEventRequestBodyJson)
  private val invalidLifeInsuranceRawRequestBody     = AnyContentAsJson(invalidLifeInsuranceRequestBodyJson)
  private val invalidCapitalRedemptionRawRequestBody = AnyContentAsJson(invalidCapitalRedemptionRequestBodyJson)
  private val invalidLifeAnnuityRawRequestBody       = AnyContentAsJson(invalidLifeAnnuityRequestBodyJson)
  private val invalidVoidedIsaRawRequestBody         = AnyContentAsJson(invalidVoidedIsaRequestBodyJson)
  private val invalidForeignRawRequestBody           = AnyContentAsJson(invalidForeignRequestBodyJson)
  private val allInvalidValueRawRequestBody          = AnyContentAsJson(allInvalidValueRequestBodyJson)

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new AmendInsurancePoliciesValidator()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)
      .anyNumberOfTimes()

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, validRawRequestBody)) shouldBe Nil
      }
    }

    // parameter format error scenarios
    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(AmendInsurancePoliciesRawData("A12344A", validTaxYear, validRawRequestBody)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, "20178", validRawRequestBody)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError error for an invalid tax year range" in new Test {
      validator.validate(AmendInsurancePoliciesRawData(validNino, "2020-22", validRawRequestBody)) shouldBe
        List(RuleTaxYearRangeInvalidError)
    }

    "return multiple errors for multiple invalid request parameters" in new Test {
      validator.validate(AmendInsurancePoliciesRawData("notValid", "2020-22", validRawRequestBody)) shouldBe
        List(NinoFormatError, RuleTaxYearRangeInvalidError)
    }

    // parameter rule error scenarios
    "return RuleTaxYearNotSupportedError error for an unsupported tax year" in new Test {
      validator.validate(AmendInsurancePoliciesRawData(validNino, "2019-20", validRawRequestBody)) shouldBe
        List(RuleTaxYearNotSupportedError)
    }

    // body format error scenarios
    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, nonsenseRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "the submitted request body is not in the correct format" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, nonValidRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/lifeInsurance/0/gainAmount"))))
      }

      "the submitted request body has missing mandatory fields" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, missingMandatoryFieldRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/foreign/1/gainAmount"))))
      }
    }

    // body value error scenarios
    "return CustomerRefFormatError error" when {
      "an incorrectly formatted customer reference is submitted" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, invalidCustomerRefRawRequestBody)) shouldBe
          List(CustomerRefFormatError.copy(paths = Some(List("/capitalRedemption/0/customerReference"))))
      }
    }

    "return EventFormatError error" when {
      "an incorrectly formatted event is submitted" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, invalidEventRawRequestBody)) shouldBe
          List(EventFormatError.copy(paths = Some(List("/lifeAnnuity/0/event"))))
      }
    }

    "return ValueFormatError error (single failure)" when {
      "one field fails value validation (life insurance)" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, invalidLifeInsuranceRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/lifeInsurance/0/gainAmount"))
            ))
      }

      "one field fails value validation (capital redemption)" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, invalidCapitalRedemptionRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/capitalRedemption/0/deficiencyRelief"))
            ))
      }

      "one field fails value validation (life annuity)" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, invalidLifeAnnuityRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/lifeAnnuity/0/gainAmount"))
            ))
      }

      "one field fails value validation (voidedIsa)" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, invalidVoidedIsaRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INTEGER_INCLUSIVE,
              paths = Some(Seq("/voidedIsa/0/yearsHeldSinceLastGain"))
            ))
      }

      "one field fails value validation (foreign)" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, invalidForeignRawRequestBody)) shouldBe
          List(
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INTEGER_INCLUSIVE,
              paths = Some(Seq("/foreign/0/yearsHeld"))
            ))
      }
    }

    "return ValueFormatError error (multiple failures)" when {
      "multiple fields fail value validation" in new Test {
        validator.validate(AmendInsurancePoliciesRawData(validNino, validTaxYear, allInvalidValueRawRequestBody)) shouldBe
          List(
            CustomerRefFormatError.copy(
              paths = Some(List(
                "/lifeInsurance/0/customerReference",
                "/capitalRedemption/0/customerReference",
                "/lifeAnnuity/1/customerReference",
                "/voidedIsa/1/customerReference",
                "/foreign/0/customerReference"
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
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(List(
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
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INTEGER_INCLUSIVE,
              paths = Some(List(
                "/lifeInsurance/0/yearsHeld",
                "/capitalRedemption/0/yearsHeld",
                "/capitalRedemption/1/yearsHeldSinceLastGain",
                "/lifeAnnuity/0/yearsHeld",
                "/voidedIsa/0/yearsHeld",
                "/voidedIsa/0/yearsHeldSinceLastGain",
                "/foreign/1/yearsHeld"
              ))
            )
          )
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors (path parameters)" in new Test {
        validator.validate(AmendInsurancePoliciesRawData("A12344A", "20178", emptyRawRequestBody)) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }

}
