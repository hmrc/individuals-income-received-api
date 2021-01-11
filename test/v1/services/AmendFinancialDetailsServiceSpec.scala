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

package v1.services

import uk.gov.hmrc.domain.Nino
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockAmendFinancialDetailsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendFinancialDetails.emploment.studentLoans.AmendStudentLoans
import v1.models.request.amendFinancialDetails.{AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}
import v1.models.request.amendFinancialDetails.emploment.{AmendBenefitsInKind, AmendDeductions, AmendEmployment, AmendPay}

import scala.concurrent.Future

class AmendFinancialDetailsServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019-20"
  private val employmentId= "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"


  private val payModel = AmendPay(
    taxablePayToDate = 3500.75,
    totalTaxToDate = 6782.92,
    tipsAndOtherPayments = Some(1024.99)
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

  trait Test extends MockAmendFinancialDetailsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AmendFinancialDetailsService = new AmendFinancialDetailsService(
      connector = mockAmendFinancialDetailsConnector
    )
  }

  "AmendFinancialDetailsService" when {
    "amendFinancialDetails" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))



        MockAmendFinancialDetailsConnector.amendFinancialDetails(request)
          .returns(Future.successful(outcome))

        await(service.amendFinancialDetails(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockAmendFinancialDetailsConnector.amendFinancialDetails(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amendFinancialDetails(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_EMPLOYMENT_ID", NotFoundError),
          ("INVALID_PAYLOAD", DownstreamError),
          ("BEFORE_TAX_YEAR_END", RuleTaxYearNotEndedError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}