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
import v1.mocks.validators.MockAmendFinancialDetailsValidator
import v1.models.request.amendFinancialDetails.emploment.studentLoans.AmendStudentLoans
import v1.models.request.amendFinancialDetails.emploment.{AmendBenefitsInKind, AmendDeductions, AmendEmployment, AmendPay}
import v1.models.request.amendFinancialDetails.{AmendFinancialDetailsRawData, AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}

class AmendFinancialDetailsRequestParserSpec extends UnitSpec {

  private val nino: String           = "AA123456B"
  private val taxYear: String        = "2020-21"
  private val employmentId           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestJson: JsValue = Json.parse(
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

  private val validRequestJsonWithOpw: JsValue = Json.parse(
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
      |        },
      |        "offPayrollWorker": true
      |    }
      |}
    """.stripMargin
  )

  private val validRawBody = AnyContentAsJson(validRequestJson)

  private val validRawBodyWithOpw = AnyContentAsJson(validRequestJsonWithOpw)

  private val amendFinancialDetailsRawDataWithOpw = AmendFinancialDetailsRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId,
    body = validRawBodyWithOpw,
    opwEnabled = true
  )

  private val amendFinancialDetailsRawData = AmendFinancialDetailsRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId,
    body = validRawBody,
    opwEnabled = false
  )

  private val payModel = AmendPay(
    taxablePayToDate = 3500.75,
    totalTaxToDate = 6782.92
  )

  private val studentLoansModel = AmendStudentLoans(
    uglDeductionAmount = Some(13343.45),
    pglDeductionAmount = Some(24242.56)
  )

  private val deductionsModel = AmendDeductions(
    studentLoans = Some(studentLoansModel)
  )

  private val benefitsInKindModel = AmendBenefitsInKind(
    accommodation = Some(455.67),
    assets = Some(435.54),
    assetTransfer = Some(24.58),
    beneficialLoan = Some(33.89),
    car = Some(3434.78),
    carFuel = Some(34.56),
    educationalServices = Some(445.67),
    entertaining = Some(434.45),
    expenses = Some(3444.32),
    medicalInsurance = Some(4542.47),
    telephone = Some(243.43),
    service = Some(45.67),
    taxableExpenses = Some(24.56),
    van = Some(56.29),
    vanFuel = Some(14.56),
    mileage = Some(34.23),
    nonQualifyingRelocationExpenses = Some(54.62),
    nurseryPlaces = Some(84.29),
    otherItems = Some(67.67),
    paymentsOnEmployeesBehalf = Some(67.23),
    personalIncidentalExpenses = Some(74.29),
    qualifyingRelocationExpenses = Some(78.24),
    employerProvidedProfessionalSubscriptions = Some(84.56),
    employerProvidedServices = Some(56.34),
    incomeTaxPaidByDirector = Some(67.34),
    travelAndSubsistence = Some(56.89),
    vouchersAndCreditCards = Some(34.90),
    nonCash = Some(23.89)
  )

  private val employmentModel = AmendEmployment(
    pay = payModel,
    deductions = Some(deductionsModel),
    benefitsInKind = Some(benefitsInKindModel),
    offPayrollWorker = None
  )

  private val employmentModelWithOpw = AmendEmployment(
    pay = payModel,
    deductions = Some(deductionsModel),
    benefitsInKind = Some(benefitsInKindModel),
    offPayrollWorker = Some(true)
  )

  private val validRequestBodyModel = AmendFinancialDetailsRequestBody(
    employment = employmentModel
  )

  private val validRequestBodyModelWithOpw = AmendFinancialDetailsRequestBody(
    employment = employmentModelWithOpw
  )

  private val amendFinancialDetailsRequest = AmendFinancialDetailsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = employmentId,
    body = validRequestBodyModel
  )

  private val amendFinancialDetailsRequestWithOpw = AmendFinancialDetailsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = employmentId,
    body = validRequestBodyModelWithOpw
  )

  trait Test extends MockAmendFinancialDetailsValidator {

    lazy val parser: AmendFinancialDetailsRequestParser = new AmendFinancialDetailsRequestParser(
      validator = mockAmendFinancialDetailsValidator
    )

  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendFinancialDetailsValidator.validate(amendFinancialDetailsRawData).returns(Nil)
        parser.parseRequest(amendFinancialDetailsRawData) shouldBe Right(amendFinancialDetailsRequest)
      }
    }

    "return a request object with opw" when {
      "valid request data with opw is supplied" in new Test {
        MockAmendFinancialDetailsValidator.validate(amendFinancialDetailsRawDataWithOpw).returns(Nil)
        parser.parseRequest(amendFinancialDetailsRawDataWithOpw) shouldBe Right(amendFinancialDetailsRequestWithOpw)
      }
    }

    "return an error when the offPayrollWorker is not provided" when {
      "the opw enabled is true and the taxYear is a TYS specific one " in new Test {
        MockAmendFinancialDetailsValidator.validate(amendFinancialDetailsRawDataWithOpw).returns(Nil)
        parser.parseRequest(amendFinancialDetailsRawDataWithOpw) shouldBe Right(amendFinancialDetailsRequestWithOpw)
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendFinancialDetailsValidator
          .validate(amendFinancialDetailsRawDataWithOpw.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(amendFinancialDetailsRawDataWithOpw.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur (NinoFormatError and TaxYearFormatError errors)" in new Test {
        MockAmendFinancialDetailsValidator
          .validate(amendFinancialDetailsRawDataWithOpw.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(amendFinancialDetailsRawDataWithOpw.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple path parameter validation errors occur (NinoFormatError, TaxYearFormatError and EmploymentIdFormatError errors)" in new Test {
        MockAmendFinancialDetailsValidator
          .validate(amendFinancialDetailsRawDataWithOpw.copy(nino = "notANino", taxYear = "notATaxYear", employmentId = "notAnEmploymentId"))
          .returns(List(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError))

        parser.parseRequest(
          amendFinancialDetailsRawDataWithOpw.copy(nino = "notANino", taxYear = "notATaxYear", employmentId = "notAnEmploymentId")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError, EmploymentIdFormatError))))
      }

      "multiple field value validation errors occur" in new Test {

        private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
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

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

        private val allInvalidValueErrors = List(
          ValueFormatError.copy(
            message = "The field should be between -99999999999.99 and 99999999999.99",
            paths = Some(List("/employment/pay/totalTaxToDate"))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
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
          )
        )

        MockAmendFinancialDetailsValidator
          .validate(amendFinancialDetailsRawDataWithOpw.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(amendFinancialDetailsRawDataWithOpw.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }

}
