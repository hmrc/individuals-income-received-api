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

package v1.connectors

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.models.request.amendFinancialDetails.emploment.studentLoans.AmendStudentLoans
import v1.models.request.amendFinancialDetails.emploment.{AmendBenefitsInKind, AmendDeductions, AmendEmployment, AmendPay}
import v1.models.request.amendFinancialDetails.{AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}

import scala.concurrent.Future

class AmendFinancialDetailsConnectorSpec extends ConnectorSpec {

  trait Test { _: ConnectorTest =>

    def taxYear: TaxYear
    val nino: String = "AA111111A"
    val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    protected val connector: AmendFinancialDetailsConnector = new AmendFinancialDetailsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val payModel = AmendPay(
      taxablePayToDate = 3500.75,
      totalTaxToDate = 6782.92
    )

    protected val studentLoansModel = AmendStudentLoans(
      uglDeductionAmount = Some(13343.45),
      pglDeductionAmount = Some(24242.56)
    )

    protected val deductionsModel = AmendDeductions(
      studentLoans = Some(studentLoansModel)
    )

    protected val benefitsInKindModel = AmendBenefitsInKind(
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

    protected val employmentModel = AmendEmployment(
      pay = payModel,
      deductions = Some(deductionsModel),
      benefitsInKind = Some(benefitsInKindModel)
    )

    protected val amendFinancialDetailsRequestBody = AmendFinancialDetailsRequestBody(
      employment = employmentModel
    )

    protected val amendFinancialDetailsRequest: AmendFinancialDetailsRequest =
      AmendFinancialDetailsRequest(Nino(nino), taxYear, employmentId, amendFinancialDetailsRequestBody)

  }

  "AmendFinancialDetailsConnector" should {
    "return a 204 status for a success scenario" when {
      "a valid request is submitted" in new Release6Test with Test {
        def taxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/income/employments/$nino/${taxYear.asMtd}/$employmentId",
          body = amendFinancialDetailsRequestBody
        ).returns(Future.successful(outcome))

        await(connector.amendFinancialDetails(amendFinancialDetailsRequest)) shouldBe outcome

      }

      "a valid request is submitted for a TYS tax year" in new TysIfsTest with Test {
        def taxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/23-24/income/employments/${nino}/${employmentId}",
          body = amendFinancialDetailsRequestBody
        ).returns(Future.successful(outcome))

        await(connector.amendFinancialDetails(amendFinancialDetailsRequest)) shouldBe outcome

      }
    }
  }

}
