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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class AmendFinancialDetailsControllerISpec extends IntegrationBaseSpec {

  "Calling the amend employment financial details endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made for a Tax Year Specific (TYS) tax year" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(offPayrollRequestBodyJson(true)))
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
            |    "employment": {
            |        "pay": {
            |            "taxablePayToDate": 3500.758,
            |            "totalTaxToDate": 6782.923
            |        },
            |        "deductions": {
            |            "studentLoans": {
            |                "uglDeductionAmount": -13343.45,
            |                "pglDeductionAmount": -24242.56
            |            }
            |        },
            |        "benefitsInKind": {
            |            "accommodation": -455.67,
            |            "assets": -435.54,
            |            "assetTransfer": -24.58,
            |            "beneficialLoan": -33.89,
            |            "car": -3434.78,
            |            "carFuel": 34.569,
            |            "educationalServices": 445.677,
            |            "entertaining": 434.458,
            |            "expenses": 3444.324,
            |            "medicalInsurance": 4542.475,
            |            "telephone": 243.436,
            |            "service": -45.67,
            |            "taxableExpenses": -24.56,
            |            "van": -56.29,
            |            "vanFuel": -14.56,
            |            "mileage": -34.23,
            |            "nonQualifyingRelocationExpenses": 54.623,
            |            "nurseryPlaces": 84.294,
            |            "otherItems": 67.676,
            |            "paymentsOnEmployeesBehalf": -67.23,
            |            "personalIncidentalExpenses": -74.29,
            |            "qualifyingRelocationExpenses": 78.244,
            |            "employerProvidedProfessionalSubscriptions": -84.56,
            |            "employerProvidedServices": -56.34,
            |            "incomeTaxPaidByDirector": 67.342,
            |            "travelAndSubsistence": -56.89,
            |            "vouchersAndCreditCards": 34.905,
            |            "nonCash": -23.89
            |        }
            |    }
            |}
          """.stripMargin
        )

        val allInvalidValueRequestError: List[MtdError] = List(
          ValueFormatError.copy(
            message = "The value must be between 0 and 99999999999.99",
            paths = Some(
              List(
                "/employment/pay/taxablePayToDate",
                "/employment/deductions/studentLoans/uglDeductionAmount",
                "/employment/deductions/studentLoans/pglDeductionAmount",
                "/employment/benefitsInKind/accommodation",
                "/employment/benefitsInKind/assets",
                "/employment/benefitsInKind/assetTransfer",
                "/employment/benefitsInKind/beneficialLoan",
                "/employment/benefitsInKind/car",
                "/employment/benefitsInKind/carFuel",
                "/employment/benefitsInKind/educationalServices",
                "/employment/benefitsInKind/entertaining",
                "/employment/benefitsInKind/expenses",
                "/employment/benefitsInKind/medicalInsurance",
                "/employment/benefitsInKind/telephone",
                "/employment/benefitsInKind/service",
                "/employment/benefitsInKind/taxableExpenses",
                "/employment/benefitsInKind/van",
                "/employment/benefitsInKind/vanFuel",
                "/employment/benefitsInKind/mileage",
                "/employment/benefitsInKind/nonQualifyingRelocationExpenses",
                "/employment/benefitsInKind/nurseryPlaces",
                "/employment/benefitsInKind/otherItems",
                "/employment/benefitsInKind/paymentsOnEmployeesBehalf",
                "/employment/benefitsInKind/personalIncidentalExpenses",
                "/employment/benefitsInKind/qualifyingRelocationExpenses",
                "/employment/benefitsInKind/employerProvidedProfessionalSubscriptions",
                "/employment/benefitsInKind/employerProvidedServices",
                "/employment/benefitsInKind/incomeTaxPaidByDirector",
                "/employment/benefitsInKind/travelAndSubsistence",
                "/employment/benefitsInKind/vouchersAndCreditCards",
                "/employment/benefitsInKind/nonCash"
              ))
          ),
          ValueFormatError.copy(
            message = "The value must be between -99999999999.99 and 99999999999.99",
            paths = Some(List("/employment/pay/totalTaxToDate"))
          )
        )

        val wrappedErrors: ErrorWrapper = errors.ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidValueRequestError)
        )

        val response: WSResponse = await(request().put(allInvalidValueRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }

      "complex error scenario" in new NonTysTest {

        val iirFinancialDetailsAmendErrorsRequest: JsValue = Json.parse(
          """
            |{
            |    "employment": {
            |        "pay": {
            |            "taxablePayToDate": 3500.758,
            |            "totalTaxToDate": 6782.929
            |        },
            |        "deductions": {
            |            "studentLoans": {
            |                "uglDeductionAmount": -13343.45,
            |                "pglDeductionAmount": -24242.56
            |            }
            |        },
            |        "benefitsInKind": {
            |            "accommodation": 455.679,
            |            "assets": 435.545,
            |            "assetTransfer": 24.582,
            |            "beneficialLoan": -33.89,
            |            "car": -3434.78,
            |            "carFuel": -34.56,
            |            "educationalServices": 445.67,
            |            "entertaining": 434.45,
            |            "expenses": 3444.32,
            |            "medicalInsurance": 4542.47,
            |            "telephone": 243.43,
            |            "service": 45.67,
            |            "taxableExpenses": 24.56,
            |            "van": 56.29,
            |            "vanFuel": 14.56,
            |            "mileage": 34.23,
            |            "nonQualifyingRelocationExpenses": 54.62,
            |            "nurseryPlaces": 84.29,
            |            "otherItems": 67.67,
            |            "paymentsOnEmployeesBehalf": 67.23,
            |            "personalIncidentalExpenses": 74.29,
            |            "qualifyingRelocationExpenses": 78.24,
            |            "employerProvidedProfessionalSubscriptions": 84.56,
            |            "employerProvidedServices": 56.34,
            |            "incomeTaxPaidByDirector": 67.34,
            |            "travelAndSubsistence": 56.89,
            |            "vouchersAndCreditCards": 34.90,
            |            "nonCash": 23.89
            |        }
            |    }
            |}
          """.stripMargin
        )

        val iirFinancialDetailsAmendErrorsResponse: JsValue = Json.parse(
          """
            |{
            |   "code":"INVALID_REQUEST",
            |   "message":"Invalid request",
            |   "errors": [
            |        {
            |            "code": "FORMAT_VALUE",
            |            "message": "The value must be between 0 and 99999999999.99",
            |            "paths": [
            |                "/employment/pay/taxablePayToDate",
            |                "/employment/deductions/studentLoans/uglDeductionAmount",
            |                "/employment/deductions/studentLoans/pglDeductionAmount",
            |                "/employment/benefitsInKind/accommodation",
            |                "/employment/benefitsInKind/assets",
            |                "/employment/benefitsInKind/assetTransfer",
            |                "/employment/benefitsInKind/beneficialLoan",
            |                "/employment/benefitsInKind/car",
            |                "/employment/benefitsInKind/carFuel"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_VALUE",
            |            "message": "The value must be between -99999999999.99 and 99999999999.99",
            |            "paths": [
            |                "/employment/pay/totalTaxToDate"
            |            ]
            |        }
            |    ]
            |}
          """.stripMargin
        )

        val response: WSResponse = await(request().put(iirFinancialDetailsAmendErrorsRequest))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe iirFinancialDetailsAmendErrorsResponse
      }
    }

    "return error according to spec" when {

      val standardRequestJson: JsValue = Json.parse(
        """
          |{
          |    "employment": {
          |        "pay": {
          |            "taxablePayToDate": 3500.75,
          |            "totalTaxToDate": 6782.92
          |        },
          |        "deductions": {
          |            "studentLoans": {
          |                "uglDeductionAmount": 13343.45,
          |                "pglDeductionAmount": 24242.56
          |            }
          |        },
          |        "benefitsInKind": {
          |            "accommodation": 455.67,
          |            "assets": 435.54,
          |            "assetTransfer": 24.58,
          |            "beneficialLoan": 33.89,
          |            "car": 3434.78,
          |            "carFuel": 34.56,
          |            "educationalServices": 445.67,
          |            "entertaining": 434.45,
          |            "expenses": 3444.32,
          |            "medicalInsurance": 4542.47,
          |            "telephone": 243.43,
          |            "service": 45.67,
          |            "taxableExpenses": 24.56,
          |            "van": 56.29,
          |            "vanFuel": 14.56,
          |            "mileage": 34.23,
          |            "nonQualifyingRelocationExpenses": 54.62,
          |            "nurseryPlaces": 84.29,
          |            "otherItems": 67.67,
          |            "paymentsOnEmployeesBehalf": 67.23,
          |            "personalIncidentalExpenses": 74.29,
          |            "qualifyingRelocationExpenses": 78.24,
          |            "employerProvidedProfessionalSubscriptions": 84.56,
          |            "employerProvidedServices": 56.34,
          |            "incomeTaxPaidByDirector": 67.34,
          |            "travelAndSubsistence": 56.89,
          |            "vouchersAndCreditCards": 34.90,
          |            "nonCash": 23.89
          |        }
          |    }
          |}
        """.stripMargin
      )

      val emptyRequestJson: JsValue = JsObject.empty

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |    "employment": {
          |        "pay": {
          |            "taxablePayToDate": true,
          |            "totalTaxToDate": false
          |        },
          |        "deductions": {
          |            "studentLoans": {
          |                "uglDeductionAmount": []
          |            }
          |        },
          |        "benefitsInKind": {
          |            "accommodation": "false"
          |        }
          |    }
          |}
        """.stripMargin
      )

      val missingEmploymentObjectRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |    "field": "value"
          |}
        """.stripMargin
      )

      val missingPayObjectRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |    "employment": {}
          |}
        """.stripMargin
      )

      val missingFieldsRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |    "employment": {
          |        "pay": {
          |        }
          |    }
          |}
        """.stripMargin
      )

      val allInvalidValueRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |    "employment": {
          |        "pay": {
          |            "taxablePayToDate": 3500.758,
          |            "totalTaxToDate": 6782.923
          |        },
          |        "deductions": {
          |            "studentLoans": {
          |                "uglDeductionAmount": -13343.45,
          |                "pglDeductionAmount": -24242.56
          |            }
          |        },
          |        "benefitsInKind": {
          |            "accommodation": -455.67,
          |            "assets": -435.54,
          |            "assetTransfer": -24.58,
          |            "beneficialLoan": -33.89,
          |            "car": -3434.78,
          |            "carFuel": 34.569,
          |            "educationalServices": 445.677,
          |            "entertaining": 434.458,
          |            "expenses": 3444.324,
          |            "medicalInsurance": 4542.475,
          |            "telephone": 243.436,
          |            "service": -45.67,
          |            "taxableExpenses": -24.56,
          |            "van": -56.29,
          |            "vanFuel": -14.56,
          |            "mileage": -34.23,
          |            "nonQualifyingRelocationExpenses": 54.623,
          |            "nurseryPlaces": 84.294,
          |            "otherItems": 67.676,
          |            "paymentsOnEmployeesBehalf": -67.23,
          |            "personalIncidentalExpenses": -74.29,
          |            "qualifyingRelocationExpenses": 78.244,
          |            "employerProvidedProfessionalSubscriptions": -84.56,
          |            "employerProvidedServices": -56.34,
          |            "incomeTaxPaidByDirector": 67.342,
          |            "travelAndSubsistence": -56.89,
          |            "vouchersAndCreditCards": 34.905,
          |            "nonCash": -23.89
          |        }
          |    }
          |}
        """.stripMargin
      )

      val invalidFieldTypeErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(
          List(
            "/employment/pay/taxablePayToDate",
            "/employment/pay/totalTaxToDate",
            "/employment/deductions/studentLoans/uglDeductionAmount",
            "/employment/benefitsInKind/accommodation"
          ))
      )

      val missingMandatoryEmploymentObjectError: MtdError = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/employment")))

      val missingMandatoryPayObjectError: MtdError = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/employment/pay")))

      val missingMandatoryFieldsErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(
          List(
            "/employment/pay/taxablePayToDate",
            "/employment/pay/totalTaxToDate"
          ))
      )

      val allInvalidValueErrors: Seq[MtdError] = Seq(
        ValueFormatError.copy(
          message = "The value must be between 0 and 99999999999.99",
          paths = Some(
            List(
              "/employment/pay/taxablePayToDate",
              "/employment/deductions/studentLoans/uglDeductionAmount",
              "/employment/deductions/studentLoans/pglDeductionAmount",
              "/employment/benefitsInKind/accommodation",
              "/employment/benefitsInKind/assets",
              "/employment/benefitsInKind/assetTransfer",
              "/employment/benefitsInKind/beneficialLoan",
              "/employment/benefitsInKind/car",
              "/employment/benefitsInKind/carFuel",
              "/employment/benefitsInKind/educationalServices",
              "/employment/benefitsInKind/entertaining",
              "/employment/benefitsInKind/expenses",
              "/employment/benefitsInKind/medicalInsurance",
              "/employment/benefitsInKind/telephone",
              "/employment/benefitsInKind/service",
              "/employment/benefitsInKind/taxableExpenses",
              "/employment/benefitsInKind/van",
              "/employment/benefitsInKind/vanFuel",
              "/employment/benefitsInKind/mileage",
              "/employment/benefitsInKind/nonQualifyingRelocationExpenses",
              "/employment/benefitsInKind/nurseryPlaces",
              "/employment/benefitsInKind/otherItems",
              "/employment/benefitsInKind/paymentsOnEmployeesBehalf",
              "/employment/benefitsInKind/personalIncidentalExpenses",
              "/employment/benefitsInKind/qualifyingRelocationExpenses",
              "/employment/benefitsInKind/employerProvidedProfessionalSubscriptions",
              "/employment/benefitsInKind/employerProvidedServices",
              "/employment/benefitsInKind/incomeTaxPaidByDirector",
              "/employment/benefitsInKind/travelAndSubsistence",
              "/employment/benefitsInKind/vouchersAndCreditCards",
              "/employment/benefitsInKind/nonCash"
            ))
        ),
        ValueFormatError.copy(
          message = "The value must be between -99999999999.99 and 99999999999.99",
          paths = Some(List("/employment/pay/totalTaxToDate"))
        )
      )

      def offPayrollRequestBodyJson(boolean: Boolean): JsValue = Json.parse(s"""
           |{
           |    "employment": {
           |        "pay": {
           |            "taxablePayToDate": 3500.75,
           |            "totalTaxToDate": 6782.92
           |        },
           |        "deductions": {
           |            "studentLoans": {
           |                "uglDeductionAmount": 13343.45,
           |                "pglDeductionAmount": 24242.56
           |            }
           |        },
           |        "benefitsInKind": {
           |            "accommodation": 455.67,
           |            "assets": 435.54,
           |            "assetTransfer": 24.58,
           |            "beneficialLoan": 33.89,
           |            "car": 3434.78,
           |            "carFuel": 34.56,
           |            "educationalServices": 445.67,
           |            "entertaining": 434.45,
           |            "expenses": 3444.32,
           |            "medicalInsurance": 4542.47,
           |            "telephone": 243.43,
           |            "service": 45.67,
           |            "taxableExpenses": 24.56,
           |            "van": 56.29,
           |            "vanFuel": 14.56,
           |            "mileage": 34.23,
           |            "nonQualifyingRelocationExpenses": 54.62,
           |            "nurseryPlaces": 84.29,
           |            "otherItems": 67.67,
           |            "paymentsOnEmployeesBehalf": 67.23,
           |            "personalIncidentalExpenses": 74.29,
           |            "qualifyingRelocationExpenses": 78.24,
           |            "employerProvidedProfessionalSubscriptions": 84.56,
           |            "employerProvidedServices": 56.34,
           |            "incomeTaxPaidByDirector": 67.34,
           |            "travelAndSubsistence": 56.89,
           |            "vouchersAndCreditCards": 34.90,
           |            "nonCash": 23.89
           |        },
           |        "offPayrollWorker": $boolean
           |    }
           |}
      """.stripMargin)

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestEmploymentId: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: ErrorWrapper,
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.error} error ${scenario.getOrElse("")}" in new NonTysTest {

            override val nino: String             = requestNino
            override val taxYear: String          = requestTaxYear
            override val employmentId: String     = requestEmploymentId
            override val requestBodyJson: JsValue = requestBody

            override def request: WSRequest = if (taxYear == "2023-24") {
              super.request().addHttpHeaders("suspend-temporal-validations" -> "true")
            } else {
              super.request()
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)

          }
        }

        val input = List(
          (
            "AA1123A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            standardRequestJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", NinoFormatError, None),
            None),
          (
            "AA123456A",
            "20199",
            "78d9f015-a8b4-47a8-8bbc-c253a1e8057e",
            standardRequestJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", TaxYearFormatError, None),
            None),
          ("AA123456A", "2019-20", "ABCDE12345FG", standardRequestJson, BAD_REQUEST, ErrorWrapper("X-123", EmploymentIdFormatError, None), None),
          (
            "AA123456A",
            "2018-19",
            "78d9f015-a8b4-47a8-8bbc-c253a1e8057e",
            standardRequestJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", RuleTaxYearNotSupportedError, None),
            None),
          (
            "AA123456A",
            "2019-21",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            standardRequestJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", RuleTaxYearRangeInvalidError, None),
            None),
          (
            "AA123456A",
            getCurrentTaxYear,
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            standardRequestJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", RuleTaxYearNotEndedError, None),
            None),
          (
            "AA123456A",
            "2019-20",
            "78d9f015-a8b4-47a8-8bbc-c253a1e8057e",
            emptyRequestJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", RuleIncorrectOrEmptyBodyError, None),
            None),
          (
            "AA123456A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            nonValidRequestBodyJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", invalidFieldTypeErrors, None),
            Some("(invalid field type)")),
          (
            "AA123456A",
            "2019-20",
            "78d9f015-a8b4-47a8-8bbc-c253a1e8057e",
            missingEmploymentObjectRequestBodyJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", missingMandatoryEmploymentObjectError, None),
            Some("(missing mandatory employment object)")),
          (
            "AA123456A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            missingPayObjectRequestBodyJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", missingMandatoryPayObjectError, None),
            Some("(missing mandatory pay object)")),
          (
            "AA123456A",
            "2019-20",
            "78d9f015-a8b4-47a8-8bbc-c253a1e8057e",
            missingFieldsRequestBodyJson,
            BAD_REQUEST,
            ErrorWrapper("X-123", missingMandatoryFieldsErrors, None),
            Some("(missing mandatory fields)")),
          (
            "AA123456A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            allInvalidValueRequestBodyJson,
            BAD_REQUEST,
            errors.ErrorWrapper("X-123", BadRequestError, Some(allInvalidValueErrors)),
            None),
          (
            "AA123456A",
            "2019-20",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            offPayrollRequestBodyJson(true),
            BAD_REQUEST,
            errors.ErrorWrapper("X-123", RuleNotAllowedOffPayrollWorker, None),
            None),
          (
            "AA123456A",
            "2023-24",
            "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            standardRequestJson,
            BAD_REQUEST,
            errors.ErrorWrapper("X-123", RuleMissingOffPayrollWorker, None),
            None)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downStream service error" when {
        def serviceErrorTest(downStreamStatus: Int, downStreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downStream returns an $downStreamCode error and status $downStreamStatus" in new NonTysTest {

            override def setupStubs(): StubMapping = {
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downStreamStatus, errorBody(downStreamCode))
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

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_EMPLOYMENT_ID", NOT_FOUND, NotFoundError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (FORBIDDEN, "BEFORE_TAX_YEAR_END", BAD_REQUEST, RuleTaxYearNotEndedError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, StandardDownstreamError)
        )

        val extraTysErrors = List(
          (BAD_REQUEST, "INCOME_SOURCE_NOT_FOUND", NOT_FOUND, NotFoundError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, StandardDownstreamError),
          (BAD_REQUEST, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String          = "AA123456A"
    val employmentId: String  = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      s"""
         |{
         |    "employment": {
         |        "pay": {
         |            "taxablePayToDate": 3500.75,
         |            "totalTaxToDate": 6782.92
         |        },
         |        "deductions": {
         |            "studentLoans": {
         |                "uglDeductionAmount": 13343.45,
         |                "pglDeductionAmount": 24242.56
         |            }
         |        },
         |        "benefitsInKind": {
         |            "accommodation": 455.67,
         |            "assets": 435.54,
         |            "assetTransfer": 24.58,
         |            "beneficialLoan": 33.89,
         |            "car": 3434.78,
         |            "carFuel": 34.56,
         |            "educationalServices": 445.67,
         |            "entertaining": 434.45,
         |            "expenses": 3444.32,
         |            "medicalInsurance": 4542.47,
         |            "telephone": 243.43,
         |            "service": 45.67,
         |            "taxableExpenses": 24.56,
         |            "van": 56.29,
         |            "vanFuel": 14.56,
         |            "mileage": 34.23,
         |            "nonQualifyingRelocationExpenses": 54.62,
         |            "nurseryPlaces": 84.29,
         |            "otherItems": 67.67,
         |            "paymentsOnEmployeesBehalf": 67.23,
         |            "personalIncidentalExpenses": 74.29,
         |            "qualifyingRelocationExpenses": 78.24,
         |            "employerProvidedProfessionalSubscriptions": 84.56,
         |            "employerProvidedServices": 56.34,
         |            "incomeTaxPaidByDirector": 67.34,
         |            "travelAndSubsistence": 56.89,
         |            "vouchersAndCreditCards": 34.90,
         |            "nonCash": 23.89
         |        }
         |    }
         |}
      """.stripMargin
    )

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
         |         "rel":"self",
         |         "method":"GET"
         |      },
         |      {
         |         "href":"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
         |         "rel":"create-and-amend-employment-financial-details",
         |         "method":"PUT"
         |      },
         |      {
         |         "href":"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
         |         "rel":"delete-employment-financial-details",
         |         "method":"DELETE"
         |      }
         |   ]
         |}
       """.stripMargin
    )

    val downstreamUri: String

    def taxYear: String

    def offPayrollRequestBodyJson(boolean: Boolean): JsValue = Json.parse(s"""
         |{
         |    "employment": {
         |        "pay": {
         |            "taxablePayToDate": 3500.75,
         |            "totalTaxToDate": 6782.92
         |        },
         |        "deductions": {
         |            "studentLoans": {
         |                "uglDeductionAmount": 13343.45,
         |                "pglDeductionAmount": 24242.56
         |            }
         |        },
         |        "benefitsInKind": {
         |            "accommodation": 455.67,
         |            "assets": 435.54,
         |            "assetTransfer": 24.58,
         |            "beneficialLoan": 33.89,
         |            "car": 3434.78,
         |            "carFuel": 34.56,
         |            "educationalServices": 445.67,
         |            "entertaining": 434.45,
         |            "expenses": 3444.32,
         |            "medicalInsurance": 4542.47,
         |            "telephone": 243.43,
         |            "service": 45.67,
         |            "taxableExpenses": 24.56,
         |            "van": 56.29,
         |            "vanFuel": 14.56,
         |            "mileage": 34.23,
         |            "nonQualifyingRelocationExpenses": 54.62,
         |            "nurseryPlaces": 84.29,
         |            "otherItems": 67.67,
         |            "paymentsOnEmployeesBehalf": 67.23,
         |            "personalIncidentalExpenses": 74.29,
         |            "qualifyingRelocationExpenses": 78.24,
         |            "employerProvidedProfessionalSubscriptions": 84.56,
         |            "employerProvidedServices": 56.34,
         |            "incomeTaxPaidByDirector": 67.34,
         |            "travelAndSubsistence": 56.89,
         |            "vouchersAndCreditCards": 34.90,
         |            "nonCash": 23.89
         |        },
         |        "offPayrollWorker": $boolean
         |    }
         |}
      """.stripMargin)

    def request(): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def uri: String = s"/employments/$nino/$taxYear/$employmentId/financial-details"

    def setupStubs(): StubMapping = StubMapping.NOT_CONFIGURED

  }

  private trait NonTysTest extends Test {
    val downstreamUri: String = s"/income-tax/income/employments/$nino/$taxYear/$employmentId"

    def taxYear: String = "2019-20"

  }

  private trait TysIfsTest extends Test {
    val downstreamUri: String = s"/income-tax/23-24/income/employments/$nino/$employmentId"

    def taxYear: String = "2023-24"

    override def request(): WSRequest =
      super.request().addHttpHeaders("suspend-temporal-validations" -> "true")

  }

}
