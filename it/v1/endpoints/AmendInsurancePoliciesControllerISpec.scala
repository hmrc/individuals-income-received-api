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

package v1.endpoints

import api.models.errors
import api.models.errors._
import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class AmendInsurancePoliciesControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String          = "AA123456A"
    val correlationId: String = "X-123"

    def taxYear: String
    def downstreamUri: String
    def uri: String = s"/insurance-policies/$nino/$taxYear"

    val requestBodyJson: JsValue = Json.parse(
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

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/insurance-policies/$nino/$taxYear",
         |         "rel":"create-and-amend-insurance-policies-income",
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
        """.stripMargin
    )

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  private trait NonTysTest extends Test {
    def taxYear: String = "2020-21"

    def downstreamUri: String = s"/income-tax/insurance-policies/income/$nino/$taxYear"

  }

  private trait TysIfsTest extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/insurance-policies/income/23-24/$nino"

  }

  "Calling 'Amend Insurance Policies' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, CREATED)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made for a TYS tax year" in new TysIfsTest {

        def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, CREATED)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new NonTysTest {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
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

        val allInvalidValueErrors: List[MtdError] = List(
          CustomerRefFormatError.copy(
            paths = Some(
              List(
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
            message = "The value must be between 0 and 99999999999.99",
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
          ValueFormatError.copy(
            message = "The value must be between 0 and 99",
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
          )
        )

        val wrappedErrors: ErrorWrapper = errors.ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidValueErrors)
        )

        def setupStubs(): StubMapping = {
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
          |           "taxPaid": true,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120,
          |           "deficiencyRelief": 5000.999
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.999,
          |           "taxPaid": true,
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
          |           "taxPaid": true,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120,
          |           "deficiencyRelief": 5000.990
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.990,
          |           "taxPaid": true,
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
          |           "taxPaid": true,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120,
          |           "deficiencyRelief": 5000.999
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.999,
          |           "taxPaid": true,
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
          |           "taxPaidAmount": 5000.999,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaidAmount": 5000.999,
          |           "yearsHeld": 150,
          |           "yearsHeldSinceLastGain": 120
          |       }
          |   ],
          |   "foreign":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "gainAmount": 2000.909,
          |           "taxPaidAmount": 5000.909,
          |           "yearsHeld": 150
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "gainAmount": -2000.99,
          |           "taxPaidAmount": -5000.99,
          |           "yearsHeld": 150
          |       }
          |   ]
          |}
        """.stripMargin
      )

      val allInvalidValueErrors: Seq[MtdError] = Seq(
        ValueFormatError.copy(
          message = "The value must be between 0 and 99999999999.99",
          paths = Some(
            List(
              "/lifeInsurance/0/gainAmount",
              "/lifeInsurance/0/deficiencyRelief",
              "/lifeInsurance/1/gainAmount",
              "/lifeInsurance/1/deficiencyRelief",
              "/capitalRedemption/1/deficiencyRelief",
              "/lifeAnnuity/0/gainAmount",
              "/lifeAnnuity/0/deficiencyRelief",
              "/lifeAnnuity/1/gainAmount",
              "/lifeAnnuity/1/deficiencyRelief",
              "/voidedIsa/0/gainAmount",
              "/voidedIsa/0/taxPaidAmount",
              "/voidedIsa/1/taxPaidAmount",
              "/foreign/0/gainAmount",
              "/foreign/0/taxPaidAmount",
              "/foreign/1/gainAmount",
              "/foreign/1/taxPaidAmount"
            ))
        ),
        ValueFormatError.copy(
          message = "The value must be between 0 and 99",
          paths = Some(
            List(
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
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": true,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
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
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": true,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
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
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaid": true,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
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
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaidAmount": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12
          |       },
          |       {
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |           "event": "Death of spouse",
          |           "gainAmount": 2000.99,
          |           "taxPaidAmount": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12
          |       }
          |   ],
          |   "foreign":[
          |       {
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |           "gainAmount": 2000.99,
          |           "taxPaidAmount": 5000.99,
          |           "yearsHeld": 15
          |       },
          |       {
          |           "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |           "gainAmount": 2000.99,
          |           "taxPaidAmount": 5000.99,
          |           "yearsHeld": 15
          |       }
          |   ]
          |}
        """.stripMargin
      )

      val customerRefFormatError: MtdError = CustomerRefFormatError.copy(
        paths = Some(
          List(
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
          |           "taxPaid": true,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
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
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": true,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
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
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaid": true,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
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
          |           "event": "",
          |           "gainAmount": 2000.99,
          |           "taxPaidAmount": 5000.99,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12
          |       },
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "",
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

      val nonValidRequestBodyJson: JsValue = Json.parse(
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

      val missingFieldRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |   "lifeInsurance":[
          |       {
          |           "customerReference": "INPOLY123A",
          |           "event": "Death of spouse",
          |           "gainAmount": 200.32,
          |           "yearsHeld": 15,
          |           "yearsHeldSinceLastGain": 12,
          |           "deficiencyRelief": 5000.99
          |       }
          |   ]
          |}
        """.stripMargin
      )

      val eventFormatError: MtdError = EventFormatError.copy(
        paths = Some(
          List(
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

      val nonValidRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq("/lifeInsurance/0/gainAmount"))
      )

      val missingFieldRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq("/lifeInsurance/0/taxPaid"))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: ErrorWrapper): Unit = {
          s"validation fails with ${expectedBody.error} error" in new NonTysTest {

            override val nino: String             = requestNino
            override val taxYear: String          = requestTaxYear
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
          ("AA1123A", "2019-20", validRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", NinoFormatError, None)),
          ("AA123456A", "20177", validRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", TaxYearFormatError, None)),
          ("AA123456A", "2018-19", validRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", RuleTaxYearNotSupportedError, None)),
          ("AA123456A", "2019-21", validRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", RuleTaxYearRangeInvalidError, None)),
          (
            "AA123456A",
            "2019-20",
            allInvalidValueRequestBodyJson,
            BAD_REQUEST,
            errors.ErrorWrapper("X-123", BadRequestError, Some(allInvalidValueErrors))),
          ("AA123456A", "2019-20", invalidCustomerRefRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", customerRefFormatError, None)),
          ("AA123456A", "2019-20", invalidEventRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", eventFormatError, None)),
          ("AA123456A", "2019-20", nonsenseRequestBody, BAD_REQUEST, ErrorWrapper("X-123", RuleIncorrectOrEmptyBodyError, None)),
          ("AA123456A", "2019-20", nonValidRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", nonValidRequestBodyErrors, None)),
          ("AA123456A", "2019-20", missingFieldRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", missingFieldRequestBodyErrors, None))
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new TysIfsTest with Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, ifsStatus, errorBody(ifsCode))
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
             |   "reason": "ifs message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (BAD_REQUEST, "INCOME_SOURCE_NOT_FOUND", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}
