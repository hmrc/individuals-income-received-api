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

package v1.models.response.retrieveFinancialDetails

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class EmploymentSpec extends UnitSpec {

  val ifsJson: JsValue = Json.parse(
    """
      |{
      |  "employmentSequenceNumber": "1002",
      |  "payrollId": "123456789999",
      |  "companyDirector": false,
      |  "closeCompany": true,
      |  "directorshipCeasedDate": "2020-02-12",
      |  "startDate": "2019-04-21",
      |  "cessationDate": "2020-03-11",
      |  "occPen": false,
      |  "disguisedRemuneration": false,
      |  "offPayrollWorker": false,
      |  "employer": {
      |    "employerRef": "223/AB12399",
      |    "employerName": "maggie"
      |  },
      |  "pay": {
      |    "taxablePayToDate": 34234.15,
      |    "totalTaxToDate": 6782.92,
      |    "payFrequency": "CALENDAR MONTHLY",
      |    "paymentDate": "2020-04-23",
      |    "taxWeekNo": 32,
      |    "taxMonthNo": 8
      |  },
      |  "estimatedPay": {
      |    "amount": 1500.99
      |  },
      |  "deductions": {
      |    "studentLoans": {
      |      "uglDeductionAmount": 13343.45,
      |      "pglDeductionAmount": 24242.56
      |    }
      |  },
      |  "benefitsInKind": {
      |    "accommodation": 455.67,
      |    "assets": 435.54,
      |    "assetTransfer": 24.58,
      |    "beneficialLoan": 33.89,
      |    "car": 3434.78,
      |    "carFuel": 34.56,
      |    "educationalServices": 445.67,
      |    "entertaining": 434.45,
      |    "expenses": 3444.32,
      |    "medicalInsurance": 4542.47,
      |    "telephone": 243.43,
      |    "service": 45.67,
      |    "taxableExpenses": 24.56,
      |    "van": 56.29,
      |    "vanFuel": 14.56,
      |    "mileage": 34.23,
      |    "nonQualifyingRelocationExpenses": 54.62,
      |    "nurseryPlaces": 84.29,
      |    "otherItems": 67.67,
      |    "paymentsOnEmployeesBehalf": 67.23,
      |    "personalIncidentalExpenses": 74.29,
      |    "qualifyingRelocationExpenses": 78.24,
      |    "employerProvidedProfessionalSubscriptions": 84.56,
      |    "employerProvidedServices": 56.34,
      |    "incomeTaxPaidByDirector": 67.34,
      |    "travelAndSubsistence": 56.89,
      |    "vouchersAndCreditCards": 34.90,
      |    "nonCash": 23.89
      |  }
      |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "employmentSequenceNumber": "1002",
      |  "payrollId": "123456789999",
      |  "companyDirector": false,
      |  "closeCompany": true,
      |  "directorshipCeasedDate": "2020-02-12",
      |  "startDate": "2019-04-21",
      |  "cessationDate": "2020-03-11",
      |  "occupationalPension": false,
      |  "disguisedRemuneration": false,
      |  "offPayrollWorker": false,
      |  "employer": {
      |    "employerRef": "223/AB12399",
      |    "employerName": "maggie"
      |  },
      |  "pay": {
      |    "taxablePayToDate": 34234.15,
      |    "totalTaxToDate": 6782.92,
      |    "payFrequency": "CALENDAR MONTHLY",
      |    "paymentDate": "2020-04-23",
      |    "taxWeekNo": 32,
      |    "taxMonthNo": 8
      |  },
      |  "customerEstimatedPay": {
      |    "amount": 1500.99
      |  },
      |  "deductions": {
      |    "studentLoans": {
      |      "uglDeductionAmount": 13343.45,
      |      "pglDeductionAmount": 24242.56
      |    }
      |  },
      |  "benefitsInKind": {
      |    "accommodation": 455.67,
      |    "assets": 435.54,
      |    "assetTransfer": 24.58,
      |    "beneficialLoan": 33.89,
      |    "car": 3434.78,
      |    "carFuel": 34.56,
      |    "educationalServices": 445.67,
      |    "entertaining": 434.45,
      |    "expenses": 3444.32,
      |    "medicalInsurance": 4542.47,
      |    "telephone": 243.43,
      |    "service": 45.67,
      |    "taxableExpenses": 24.56,
      |    "van": 56.29,
      |    "vanFuel": 14.56,
      |    "mileage": 34.23,
      |    "nonQualifyingRelocationExpenses": 54.62,
      |    "nurseryPlaces": 84.29,
      |    "otherItems": 67.67,
      |    "paymentsOnEmployeesBehalf": 67.23,
      |    "personalIncidentalExpenses": 74.29,
      |    "qualifyingRelocationExpenses": 78.24,
      |    "employerProvidedProfessionalSubscriptions": 84.56,
      |    "employerProvidedServices": 56.34,
      |    "incomeTaxPaidByDirector": 67.34,
      |    "travelAndSubsistence": 56.89,
      |    "vouchersAndCreditCards": 34.90,
      |    "nonCash": 23.89
      |  }
      |}
    """.stripMargin
  )

  val model: Employment = Employment(
    employmentSequenceNumber = Some("1002"),
    payrollId = Some("123456789999"),
    companyDirector = Some(false),
    closeCompany = Some(true),
    directorshipCeasedDate = Some("2020-02-12"),
    startDate = Some("2019-04-21"),
    cessationDate = Some("2020-03-11"),
    occupationalPension = Some(false),
    disguisedRemuneration = Some(false),
    offPayrollWorker = Some(false),
    employer = Employer(
      employerRef = Some("223/AB12399"),
      employerName = "maggie"
    ),
    pay = Some(
      Pay(
        taxablePayToDate = Some(34234.15),
        totalTaxToDate = Some(6782.92),
        payFrequency = Some("CALENDAR MONTHLY"),
        paymentDate = Some("2020-04-23"),
        taxWeekNo = Some(32),
        taxMonthNo = Some(8)
      )),
    customerEstimatedPay = Some(
      CustomerEstimatedPay(
        amount = Some(1500.99)
      )),
    deductions = Some(
      Deductions(
        Some(
          StudentLoans(
            uglDeductionAmount = Some(13343.45),
            pglDeductionAmount = Some(24242.56)
          ))
      )),
    benefitsInKind = Some(
      BenefitsInKind(
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
      ))
  )

  "Employment" when {
    "read from valid JSON" should {
      "produce the expected 'Employment' object" in {
        ifsJson.as[Employment] shouldBe model
      }
    }

    "read from JSON with empty field objects" should {
      "produce the expected 'Employment' object" in {
        val json: JsValue = Json.parse(
          """
            |{
            |  "employmentSequenceNumber": "1002",
            |  "payrollId": "123456789999",
            |  "companyDirector": false,
            |  "closeCompany": true,
            |  "directorshipCeasedDate": "2020-02-12",
            |  "startDate": "2019-04-21",
            |  "cessationDate": "2020-03-11",
            |  "occPen": false,
            |  "disguisedRemuneration": false,
            |  "offPayrollWorker": false,
            |  "employer": {
            |    "employerRef": "223/AB12399",
            |    "employerName": "maggie"
            |  },
            |  "pay": {
            |    "taxablePayToDate": 34234.15,
            |    "totalTaxToDate": 6782.92,
            |    "payFrequency": "CALENDAR MONTHLY",
            |    "paymentDate": "2020-04-23",
            |    "taxWeekNo": 32,
            |    "taxMonthNo": 8
            |  },
            |  "estimatedPay": {
            |  },
            |  "deductions": {
            |    "studentLoans": {
            |    }
            |  },
            |  "benefitsInKind": {
            |  }
            |}
          """.stripMargin
        )

        json.as[Employment] shouldBe model.copy(
          customerEstimatedPay = None,
          deductions = None,
          benefitsInKind = None
        )
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson: JsValue = Json.parse(
          """
            |{
            |  "employerRef": "anEmployerReference"
            |}
          """.stripMargin
        )

        invalidJson.validate[Employment] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

}
