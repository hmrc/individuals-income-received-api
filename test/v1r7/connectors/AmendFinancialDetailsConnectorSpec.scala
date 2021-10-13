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

package v1r7.connectors

import mocks.MockAppConfig
import v1r7.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1r7.mocks.MockHttpClient
import v1r7.models.outcomes.ResponseWrapper
import v1r7.models.request.amendFinancialDetails.emploment.studentLoans.AmendStudentLoans
import v1r7.models.request.amendFinancialDetails.emploment.{AmendBenefitsInKind, AmendDeductions, AmendEmployment, AmendPay}
import v1r7.models.request.amendFinancialDetails.{AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}

import scala.concurrent.Future

class AmendFinancialDetailsConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA111111A"
  private val taxYear: String = "2019-20"
  private val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

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
    benefitsInKind = Some(benefitsInKindModel)
  )

  private val requestBody = AmendFinancialDetailsRequestBody(
    employment = employmentModel
  )

  val request: AmendFinancialDetailsRequest = AmendFinancialDetailsRequest(Nino(nino), taxYear, employmentId, requestBody)

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendFinancialDetailsConnector = new AmendFinancialDetailsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.ifsBaseUrl returns baseUrl
    MockedAppConfig.ifsToken returns "ifs-token"
    MockedAppConfig.ifsEnvironment returns "ifs-environment"
    MockedAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "AmendFinancialDetailsConnector" should {
    "return a 204 status for a success scenario" when {
      "a valid request is submitted" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredIfsHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/income/employments/$nino/$taxYear/$employmentId",
            config = dummyIfsHeaderCarrierConfig,
            body = requestBody,
            requiredHeaders = requiredIfsHeadersPut,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))

        await(connector.amendFinancialDetails(request)) shouldBe outcome
      }
    }
  }
}