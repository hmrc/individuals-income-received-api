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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.domain.DesTaxYear
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendInsurancePoliciesControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2017-18"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |   "lifeInsurance":[
        |       {
        |           "customerReference": "INPOLY123A",
        |           "event": "Death of spouse",
        |           "gainAmount": 2000.99,
        |           "taxPaid": 5000.99,
        |           "yearsHeld": 15,
        |           "yearsHeldSinceLastGain": 12,
        |           "deficiencyRelief": 5000.99
        |       },
        |       {
        |           "customerReference": "INPOLY123A",
        |           "event": "Death of spouse",
        |           "gainAmount": 2000.99,
        |           "taxPaid": 5000.99,
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
        |           "taxPaid": 5000.99,
        |           "yearsHeld": 15,
        |           "yearsHeldSinceLastGain": 12,
        |           "deficiencyRelief": 5000.99
        |       },
        |       {
        |           "customerReference": "INPOLY123A",
        |           "event": "Death of spouse",
        |           "gainAmount": 2000.99,
        |           "taxPaid": 5000.99,
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
        |           "taxPaid": 5000.99,
        |           "yearsHeld": 15,
        |           "yearsHeldSinceLastGain": 12,
        |           "deficiencyRelief": 5000.99
        |       },
        |       {
        |           "customerReference": "INPOLY123A",
        |           "event": "Death of spouse",
        |           "gainAmount": 2000.99,
        |           "taxPaid": 5000.99,
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
        |           "taxPaid": 5000.99,
        |           "yearsHeld": 15,
        |           "yearsHeldSinceLastGain": 12
        |       },
        |       {
        |           "customerReference": "INPOLY123A",
        |           "event": "Death of spouse",
        |           "gainAmount": 2000.99,
        |           "taxPaid": 5000.99,
        |           "yearsHeld": 15,
        |           "yearsHeldSinceLastGain": 12
        |       }
        |   ],
        |   "foreign":[
        |       {
        |           "customerReference": "INPOLY123A",
        |           "gainAmount": 2000.99,
        |           "taxPaid": 5000.99,
        |           "yearsHeld": 15
        |       },
        |       {
        |           "customerReference": "INPOLY123A",
        |           "gainAmount": 2000.99,
        |           "taxPaid": 5000.99,
        |           "yearsHeld": 15
        |       }
        |   ]
        |}
    """.stripMargin
    )

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/insurance-policies/$nino/$taxYear",
         |         "rel":"amend-insurance-policies-income",
         |         "method":"PUT"
         |      },
         |      {
         |         "href":"/individuals/income-received/insurance-policies/$nino/$taxYear",
         |         "rel":"self",
         |         "method":"GET"
         |      },
         |      {
         |         "href":"/individuals/income-received/insurance-policies/$nino/$taxYear",
         |         "rel":"delete-insurance-policies-income",
         |         "method":"DELETE"
         |      }
         |   ]
         |}
         |""".stripMargin
    )

    def uri: String = s"/insurance-policies/$nino/$taxYear"

    def desUri: String = s"/some-placeholder/insurance-policies/$nino/${DesTaxYear.fromMtd(taxYear)}"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling 'Amend Insurance Policies' endpoint"should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new Test {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            {
            |   "lifeInsurance":[
            |       {
            |           "customerReference": "This ref is more than 25 characters",
            |           "event": "Death of spouse",
            |           "gainAmount": 2000.999,
            |           "taxPaid": 5000.999,
            |           "yearsHeld": -15,
            |           "yearsHeldSinceLastGain": 12,
            |           "deficiencyRelief": 5000.999
            |       },
            |       {
            |           "customerReference": "INPOLY123A",
            |           "event": "This event string is 76 characters long --------------------------------- 76",
            |           "gainAmount": 2000.99,
            |           "taxPaid": 5000.99,
            |           "yearsHeld": 15,
            |           "yearsHeldSinceLastGain": 12,
            |           "deficiencyRelief": 5000.99
            |       }
            |   ],
            |   "capitalRedemption":[
            |       {
            |           "customerReference": "This ref is more than 25 characters",
            |           "event": "Death of spouse",
            |           "gainAmount": 3000.999,
            |           "taxPaid": 5000.99,
            |           "yearsHeld": -15,
            |           "yearsHeldSinceLastGain": 12,
            |           "deficiencyRelief": 5000.99
            |       },
            |       {
            |           "customerReference": "INPOLY123A",
            |           "event": "Death of spouse",
            |           "gainAmount": 2000.99,
            |           "taxPaid": 5000.999,
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
            |           "taxPaid": 5000.999,
            |           "yearsHeld": -15,
            |           "yearsHeldSinceLastGain": 12,
            |           "deficiencyRelief": 5000.999
            |       },
            |       {
            |           "customerReference": "This ref is more than 25 characters",
            |           "event": "This event string is 76 characters long --------------------------------- 76",
            |           "gainAmount": 5000.99,
            |           "taxPaid": 5000.99,
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
            |           "taxPaid": 5000.99,
            |           "yearsHeld": -15,
            |           "yearsHeldSinceLastGain": 120
            |       },
            |       {
            |           "customerReference": "This ref is more than 25 characters",
            |           "event": "Death of spouse",
            |           "gainAmount": 5000.999,
            |           "taxPaid": 5000.999,
            |           "yearsHeld": 15,
            |           "yearsHeldSinceLastGain": 12
            |       }
            |   ],
            |   "foreign":[
            |       {
            |           "customerReference": "This ref is more than 25 characters",
            |           "gainAmount": 5000.99,
            |           "taxPaid": 5000.999,
            |           "yearsHeld": 15
            |       },
            |       {
            |           "customerReference": "INPOLY123A",
            |           "gainAmount": 2000.999,
            |           "taxPaid": 5000.99,
            |           "yearsHeld": -15
            |       }
            |   ]
            |}
          """.stripMargin
        )

        val allInvalidValueErrors: List[MtdError] = List(
          CustomerRefFormatError.copy(
            paths = Some(List(
              "/lifeInsurance/0/customerReference",
              "/capitalRedemption/0/customerReference",
              "/lifeAnnuity/1/customerReference",
              "/voidedIsa/1/customerReference",
              "/foreign/0/customerReference"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0.01 and 99999999999.99",
            paths = Some(List(
              "/lifeInsurance/0/gainAmount",
              "/lifeInsurance/0/deficiencyRelief",
              "/capitalRedemption/0/gainAmount",
              "/capitalRedemption/1/deficiencyRelief",
              "/lifeAnnuity/0/deficiencyRelief",
              "/foreign/1/gainAmount"
            ))
          ),
          EventFormatError.copy(
            paths = Some(List(
              "/lifeInsurance/1/event",
              "/lifeAnnuity/1/event"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99",
            paths = Some(List(
              "/lifeInsurance/0/yearsHeld",
              "/capitalRedemption/0/yearsHeld",
              "/capitalRedemption/1/yearsHeldSinceLastGain",
              "/lifeAnnuity/0/yearsHeld",
              "/voidedIsa/0/yearsHeld",
              "/voidedIsa/0/yearsHeldSinceLastGain",
              "/foreign/1/yearsHeld"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(List(
              "/lifeInsurance/0/taxPaid",
              "/capitalRedemption/1/taxPaid",
              "/lifeAnnuity/0/taxPaid",
              "/voidedIsa/1/gainAmount",
              "/voidedIsa/1/taxPaid",
              "/foreign/0/taxPaid",
            ))
          )
        )


        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = Some(correlationId),
          error = BadRequestError,
          errors = Some(allInvalidValueErrors)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(allInvalidValueRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }
    }

    "return error according to spec" when {

      val validRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "lifeInsurance":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
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
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
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
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
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
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12
          |       }
          |   ],
          |   "foreign":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15
          |       }
          |   ]
          |}
    """.stripMargin
      )

      val nonsenseRequestBody: JsValue = Json.parse(
        """
          |{
          |  "field": "value"
          |}
        """.stripMargin
      )

      val allInvalidValueRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "lifeInsurance":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": -2000.99,
          |           "taxPaid": 5000.999,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120,
          |           "deficiencyRelief": 5000.999
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.999,
          |           "taxPaid": -5000.99,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120,
          |           "deficiencyRelief": 5000.999
          |       }
          |   ],
          |   "capitalRedemption":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.999,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120,
          |           "deficiencyRelief": 5000.990
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.990,
          |           "taxPaid": -5000.99,
          |           "yearsHeld": -15,
          |           "yearsHeldSinceLastGain": -12,
          |           "deficiencyRelief": -5000.99
          |       }
          |   ],
          |   "lifeAnnuity":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": -2000.99,
          |           "taxPaid": -5000.99,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120,
          |           "deficiencyRelief": 5000.999
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.999,
          |           "taxPaid": 5000.999,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120,
          |           "deficiencyRelief": 5000.999
          |       }
          |   ],
          |   "voidedIsa":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.999,
          |           "taxPaid": 5000.999,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.999,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120
          |       }
          |   ],
          |   "foreign":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "gainAmount": 2000.909,
          |           "taxPaid": 5000.909,
          |           "yearsHeld": 150
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "gainAmount": -2000.99,
          |           "taxPaid": -5000.99,
          |           "yearsHeld": 150
          |       }
          |   ]
          |}
        """.stripMargin
      )

      val allInvalidValueErrors: Seq[MtdError] = Seq(
        ValueFormatError.copy(
          message = "The field should be between 0 and 99999999999.99",
          paths = Some(List(
            "/lifeInsurance/0/taxPaid",
            "/lifeInsurance/1/taxPaid",
            "/capitalRedemption/0/taxPaid",
            "/capitalRedemption/1/taxPaid",
            "/lifeAnnuity/0/taxPaid",
            "/lifeAnnuity/1/taxPaid",
            "/voidedIsa/0/gainAmount",
            "/voidedIsa/0/taxPaid",
            "/voidedIsa/1/taxPaid",
            "/foreign/0/taxPaid",
            "/foreign/1/taxPaid"
          ))
        ),
        ValueFormatError.copy(
          message = "The field should be between 0.01 and 99999999999.99",
          paths = Some(List(
            "/lifeInsurance/0/gainAmount",
            "/lifeInsurance/0/deficiencyRelief",
            "/lifeInsurance/1/gainAmount",
            "/lifeInsurance/1/deficiencyRelief",
            "/capitalRedemption/1/deficiencyRelief",
            "/lifeAnnuity/0/gainAmount",
            "/lifeAnnuity/0/deficiencyRelief",
            "/lifeAnnuity/1/gainAmount",
            "/lifeAnnuity/1/deficiencyRelief",
            "/foreign/0/gainAmount",
            "/foreign/1/gainAmount"
          ))
        ),
        ValueFormatError.copy(
          message = "The field should be between 0 and 99",
          paths = Some(List(
            "/lifeInsurance/0/yearsHeld",
            "/lifeInsurance/0/yearsHeldSinceLastGain",
            "/lifeInsurance/1/yearsHeld",
            "/lifeInsurance/1/yearsHeldSinceLastGain",
            "/capitalRedemption/0/yearsHeld",
            "/capitalRedemption/0/yearsHeldSinceLastGain",
            "/capitalRedemption/1/yearsHeld",
            "/capitalRedemption/1/yearsHeldSinceLastGain",
            "/lifeAnnuity/0/yearsHeld",
            "/lifeAnnuity/0/yearsHeldSinceLastGain",
            "/lifeAnnuity/1/yearsHeld",
            "/lifeAnnuity/1/yearsHeldSinceLastGain",
            "/voidedIsa/0/yearsHeld",
            "/voidedIsa/0/yearsHeldSinceLastGain",
            "/voidedIsa/1/yearsHeld",
            "/voidedIsa/1/yearsHeldSinceLastGain",
            "/foreign/0/yearsHeld",
            "/foreign/1/yearsHeld"
          ))
        )
      )

      val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "lifeInsurance":[
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       }
          |   ],
          |   "capitalRedemption":[
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       }
          |   ],
          |   "lifeAnnuity":[
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       }
          |   ],
          |   "voidedIsa":[
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12
          |       },
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12
          |       }
          |   ],
          |   "foreign":[
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15
          |       },
          |       {
          |           "customerReference": "This ref is more than 25 characters",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15
          |       }
          |   ]
          |}
        """.stripMargin
      )

      val customerRefFormatError: MtdError = CustomerRefFormatError.copy(
          paths = Some(List(
            "/lifeInsurance/0/customerReference",
            "/lifeInsurance/1/customerReference",

            "/capitalRedemption/0/customerReference",
            "/capitalRedemption/1/customerReference",

            "/lifeAnnuity/0/customerReference",
            "/lifeAnnuity/1/customerReference",

            "/voidedIsa/0/customerReference",
            "/voidedIsa/1/customerReference",

            "/foreign/0/customerReference",
            "/foreign/1/customerReference"
          ))
        )

      val invalidEventRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "lifeInsurance":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       }
          |   ],
          |   "capitalRedemption":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       }
          |   ],
          |   "lifeAnnuity":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       }
          |   ],
          |   "voidedIsa":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12
          |       }
          |   ],
          |   "foreign":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "gainAmount": 2000.99,
          |           "taxPaid": 5000.99,
          |           "yearsHeld": 15
          |       }
          |   ]
          |}
        """.stripMargin
      )

      val eventFormatError: MtdError = EventFormatError.copy(
          paths = Some(List(
            "/lifeInsurance/0/event",
            "/lifeInsurance/1/event",

            "/capitalRedemption/0/event",
            "/capitalRedemption/1/event",

            "/lifeAnnuity/0/event",
            "/lifeAnnuity/1/event",

            "/voidedIsa/0/event",
            "/voidedIsa/1/event"
          ))
        )

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, requestBody: JsValue, expectedStatus: Int, expectedBody: ErrorWrapper): Unit = {
          s"validation fails with ${expectedBody.error} error" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear
            override val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2017-18", validRequestBodyJson, BAD_REQUEST, ErrorWrapper(Some(""), NinoFormatError, None) ),
          ("AA123456A", "20177", validRequestBodyJson,  BAD_REQUEST, ErrorWrapper(Some(""), TaxYearFormatError, None)),
          ("AA123456A", "2015-17", validRequestBodyJson, BAD_REQUEST, ErrorWrapper(Some(""), RuleTaxYearRangeInvalidError, None) ),
          ("AA123456A", "2017-18", allInvalidValueRequestBodyJson, BAD_REQUEST, ErrorWrapper(Some(""), BadRequestError, Some(allInvalidValueErrors))),
          ("AA123456A", "2017-18", invalidCustomerRefRequestBodyJson, BAD_REQUEST, ErrorWrapper(Some(""), customerRefFormatError, None)),
          ("AA123456A", "2017-18", invalidEventRequestBodyJson, BAD_REQUEST, ErrorWrapper(Some(""), eventFormatError, None)),
          ("AA123456A", "2017-18", nonsenseRequestBody, BAD_REQUEST, ErrorWrapper(Some(""), RuleIncorrectOrEmptyBodyError, None)))

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.PUT, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "des message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_NINO", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
