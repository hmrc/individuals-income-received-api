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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendFinancialDetailsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"
    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |    "employment": {
        |        "pay": {
        |            "taxablePayToDate": 3500.75,
        |            "totalTaxToDate": 6782.92,
        |            "tipsAndOtherPayments": 1024.99
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

    def uri: String = s"/employments/$nino/$taxYear/$employmentId/financial-details"

    def desUri: String = s"/income-tax/income/employments/$nino/$taxYear/$employmentId"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the amend employment financial details endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT)
        }

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
            |{
            |    "employment": {
            |        "pay": {
            |            "taxablePayToDate": 3500.758,
            |            "totalTaxToDate": 6782.923,
            |            "tipsAndOtherPayments": 1024.994
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
            message = "The field should be between -99999999999.99 and 99999999999.99",
            paths = Some(List("/employment/pay/totalTaxToDate"))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(List(
              "/employment/pay/taxablePayToDate",
              "/employment/pay/tipsAndOtherPayments",
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
          )
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidValueRequestError)
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

      "complex error scenario" in new Test {

        val iirFinancialDetailsAmendErrorsRequest: JsValue = Json.parse(
          """
            |{
            |    "employment": {
            |        "pay": {
            |            "taxablePayToDate": 3500.758,
            |            "totalTaxToDate": 6782.929,
            |            "tipsAndOtherPayments": 1024.999
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
            |            "message": "The field should be between -99999999999.99 and 99999999999.99",
            |            "paths": [
            |                "/employment/pay/totalTaxToDate"
            |            ]
            |        },
            |        {
            |            "code": "FORMAT_VALUE",
            |            "message": "The field should be between 0 and 99999999999.99",
            |            "paths": [
            |                "/employment/pay/taxablePayToDate",
            |                "/employment/pay/tipsAndOtherPayments",
            |                "/employment/deductions/studentLoans/uglDeductionAmount",
            |                "/employment/deductions/studentLoans/pglDeductionAmount",
            |                "/employment/benefitsInKind/accommodation",
            |                "/employment/benefitsInKind/assets",
            |                "/employment/benefitsInKind/assetTransfer",
            |                "/employment/benefitsInKind/beneficialLoan",
            |                "/employment/benefitsInKind/car",
            |                "/employment/benefitsInKind/carFuel"
            |            ]
            |        }
            |    ]
            |}
          """.stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(iirFinancialDetailsAmendErrorsRequest))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe iirFinancialDetailsAmendErrorsResponse
      }
    }

    "return error according to spec" when {

      val validRequestJson: JsValue = Json.parse(
        """
          |{
          |    "employment": {
          |        "pay": {
          |            "taxablePayToDate": 3500.75,
          |            "totalTaxToDate": 6782.92,
          |            "tipsAndOtherPayments": 1024.99
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
          |            "totalTaxToDate": false,
          |            "tipsAndOtherPayments": "true"
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
          |            "tipsAndOtherPayments": 1024.99
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
          |            "totalTaxToDate": 6782.923,
          |            "tipsAndOtherPayments": 1024.994
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
        paths = Some(List(
          "/employment/deductions/studentLoans/uglDeductionAmount",
          "/employment/pay/tipsAndOtherPayments",
          "/employment/pay/totalTaxToDate",
          "/employment/pay/taxablePayToDate",
          "/employment/benefitsInKind/accommodation",
        ))
      )

      val missingMandatoryEmploymentObjectError: MtdError = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/employment")))

      val missingMandatoryPayObjectError: MtdError = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/employment/pay")))

      val missingMandatoryFieldsErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(List(
          "/employment/pay/taxablePayToDate",
          "/employment/pay/totalTaxToDate"
        ))
      )

      val allInvalidValueErrors: Seq[MtdError] = Seq(
        ValueFormatError.copy(
          message = "The field should be between -99999999999.99 and 99999999999.99",
          paths = Some(List("/employment/pay/totalTaxToDate"))
        ),
        ValueFormatError.copy(
          message = "The field should be between 0 and 99999999999.99",
          paths = Some(List(
            "/employment/pay/taxablePayToDate",
            "/employment/pay/tipsAndOtherPayments",
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
        )
      )

      def getCurrentTaxYear: String = {
        val currentDate = DateTime.now(DateTimeZone.UTC)

        val taxYearStartDate: DateTime = DateTime.parse(
          currentDate.getYear + "-04-06",
          DateTimeFormat.forPattern("yyyy-MM-dd")
        )

        def fromDesIntToString(taxYear: Int): String =
          (taxYear - 1) + "-" + taxYear.toString.drop(2)

        if (currentDate.isBefore(taxYearStartDate)){
          fromDesIntToString(currentDate.getYear)
        }
        else {
          fromDesIntToString(currentDate.getYear + 1)
        }
      }

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, requestEmploymentId: String, requestBody: JsValue, expectedStatus: Int,
                                expectedBody: ErrorWrapper, scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.error} error ${scenario.getOrElse("")}" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear
            override val employmentId: String = requestEmploymentId
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
          ("AA1123A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", validRequestJson, BAD_REQUEST, ErrorWrapper("X-123", NinoFormatError, None), None),
          ("AA123456A", "20199", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", validRequestJson, BAD_REQUEST, ErrorWrapper("X-123", TaxYearFormatError, None), None),
          ("AA123456A", "2019-20", "ABCDE12345FG", validRequestJson, BAD_REQUEST, ErrorWrapper("X-123", EmploymentIdFormatError, None), None),
          ("AA123456A", "2018-19", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", validRequestJson, BAD_REQUEST, ErrorWrapper("X-123", RuleTaxYearNotSupportedError, None), None),
          ("AA123456A", "2019-21", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", validRequestJson, BAD_REQUEST, ErrorWrapper("X-123", RuleTaxYearRangeInvalidError, None), None),
          ("AA123456A", getCurrentTaxYear, "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", validRequestJson, BAD_REQUEST, ErrorWrapper("X-123", RuleTaxYearNotEndedError, None), None),
          ("AA123456A", "2019-20", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", emptyRequestJson, BAD_REQUEST, ErrorWrapper("X-123", RuleIncorrectOrEmptyBodyError, None), None),
          ("AA123456A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", nonValidRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", invalidFieldTypeErrors, None), Some("(invalid field type)")),
          ("AA123456A", "2019-20", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", missingEmploymentObjectRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", missingMandatoryEmploymentObjectError, None), Some("(missing mandatory employment object)")),
          ("AA123456A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", missingPayObjectRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", missingMandatoryPayObjectError, None), Some("(missing mandatory pay object)")),
          ("AA123456A", "2019-20", "78d9f015-a8b4-47a8-8bbc-c253a1e8057e", missingFieldsRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", missingMandatoryFieldsErrors, None), Some("(missing mandatory fields)")),
          ("AA123456A", "2019-20", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", allInvalidValueRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", BadRequestError, Some(allInvalidValueErrors)), None)
        )

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
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_EMPLOYMENT_ID", NOT_FOUND, NotFoundError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
          (FORBIDDEN, "BEFORE_TAX_YEAR_END", BAD_REQUEST, RuleTaxYearNotEndedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}