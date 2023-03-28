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

package v1

import api.models.errors.ErrorWrapper
import api.models.outcomes.ResponseWrapper
import v1.models.response.addCustomEmployment.AddCustomEmploymentResponse
import v1.models.response.addUkSavingsAccount.AddUkSavingsAccountResponse
import v1.models.response.listEmployment.{Employment, ListEmploymentResponse}
import v1.models.response.listUkSavingsAccounts.{ListUkSavingsAccountsResponse, UkSavingsAccount}
import v1.models.response.retrieveAllResidentialPropertyCgt.RetrieveAllResidentialPropertyCgtResponse
import v1.models.response.retrieveDividends.RetrieveDividendsResponse
import v1.models.response.retrieveEmployment.RetrieveEmploymentResponse
import v1.models.response.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsResponse
import v1.models.response.retrieveForeign.RetrieveForeignResponse
import v1.models.response.retrieveInsurancePolicies.RetrieveInsurancePoliciesResponse
import v1.models.response.retrieveNonPayeEmploymentIncome.RetrieveNonPayeEmploymentIncomeResponse
import v1.models.response.retrieveOther.RetrieveOtherResponse
import v1.models.response.retrieveOtherCgt.RetrieveOtherCgtResponse
import v1.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse
import v1.models.response.retrievePensions.RetrievePensionsResponse
import v1.models.response.retrieveSavings.RetrieveSavingsResponse
import v1.models.response.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryResponse
import v1.models.response.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryResponse

package object services {

  type ServiceOutcome[A] = Either[ErrorWrapper, ResponseWrapper[A]]

  // Pensions
  type CreateAmendPensionServiceOutcome = ServiceOutcome[Unit]
  type RetrievePensionsServiceOutcome   = ServiceOutcome[RetrievePensionsResponse]
  type DeletePensionsServiceOutcome     = ServiceOutcome[Unit]

  // Savings
  type CreateAmendSavingsServiceOutcome = ServiceOutcome[Unit]
  type RetrieveSavingsServiceOutcome    = ServiceOutcome[RetrieveSavingsResponse]
  type DeleteSavingsServiceOutcome      = ServiceOutcome[Unit]

  // Employments
  type AddCustomEmploymentServiceOutcome                   = ServiceOutcome[AddCustomEmploymentResponse]
  type AmendCustomEmploymentServiceOutcome                 = ServiceOutcome[Unit]
  type AmendFinancialDetailsServiceOutcome                 = ServiceOutcome[Unit]
  type DeleteCustomEmploymentServiceOutcome                = ServiceOutcome[Unit]
  type DeleteEmploymentFinancialDetailsServiceOutcome      = ServiceOutcome[Unit]
  type ListEmploymentsServiceOutcome                       = ServiceOutcome[ListEmploymentResponse[Employment]]
  type RetrieveEmploymentAndFinancialDetailsServiceOutcome = ServiceOutcome[RetrieveEmploymentAndFinancialDetailsResponse]
  type RetrieveEmploymentServiceOutcome                    = ServiceOutcome[RetrieveEmploymentResponse]

  // Non-PAYE Employment Income
  type CreateAmendNonPayeEmploymentServiceOutcome = ServiceOutcome[Unit]
  type DeleteNonPayeEmploymentServiceOutcome      = ServiceOutcome[Unit]
  type RetrieveNonPayeEmploymentServiceOutcome    = ServiceOutcome[RetrieveNonPayeEmploymentIncomeResponse]

  // Other Employment Income
  type AmendOtherEmploymentServiceOutcome          = ServiceOutcome[Unit]
  type DeleteOtherEmploymentIncomeServiceOutcome   = ServiceOutcome[Unit]
  type RetrieveOtherEmploymentIncomeServiceOutcome = ServiceOutcome[RetrieveOtherEmploymentResponse]

  // Other Income
  type CreateAmendOtherServiceOutcome = ServiceOutcome[Unit]
  type DeleteOtherServiceOutcome      = ServiceOutcome[Unit]
  type RetrieveOtherServiceOutcome    = ServiceOutcome[RetrieveOtherResponse]

  // Foreign Income
  type AmendForeignServiceOutcome    = ServiceOutcome[Unit]
  type DeleteForeignServiceOutcome   = ServiceOutcome[Unit]
  type RetrieveForeignServiceOutcome = ServiceOutcome[RetrieveForeignResponse]

  // Insurance Policies Income
  type AmendInsurancePoliciesServiceOutcome    = ServiceOutcome[Unit]
  type DeleteInsurancePoliciesServiceOutcome   = ServiceOutcome[Unit]
  type RetrieveInsurancePoliciesServiceOutcome = ServiceOutcome[RetrieveInsurancePoliciesResponse]

  // UK Savings Account
  type AddUkSavingsAccountServiceOutcome                   = ServiceOutcome[AddUkSavingsAccountResponse]
  type CreateAmendUkSavingsAnnualSummaryServiceOutcome     = ServiceOutcome[Unit]
  type ListUkSavingsAccountsServiceOutcome                 = ServiceOutcome[ListUkSavingsAccountsResponse[UkSavingsAccount]]
  type RetrieveUkSavingsAccountAnnualSummaryServiceOutcome = ServiceOutcome[RetrieveUkSavingsAnnualSummaryResponse]

  // Dividends Income
  type AmendDividendsServiceOutcome    = ServiceOutcome[Unit]
  type DeleteDividendsServiceOutcome   = ServiceOutcome[Unit]
  type RetrieveDividendsServiceOutcome = ServiceOutcome[RetrieveDividendsResponse]

  // UK Dividends
  type CreateAmendUkDividendsServiceOutcome = ServiceOutcome[Unit]
  type RetrieveUkDividendsServiceOutcome    = ServiceOutcome[RetrieveUkDividendsAnnualIncomeSummaryResponse]
  type DeleteUkDividendsServiceOutcome      = ServiceOutcome[Unit]

  // Capital Gains Tax
  type CreateAmendCgtPpdOverridesServiceOutcome                 = ServiceOutcome[Unit]
  type CreateAmendOtherCgtServiceOutcome                        = ServiceOutcome[Unit]
  type CreateAmendCgtResidentialPropertyDisposalsServiceOutcome = ServiceOutcome[Unit]
  type DeleteCgtNonPpdServiceOutcome                            = ServiceOutcome[Unit]
  type DeleteOtherCgtServiceOutcome                             = ServiceOutcome[Unit]
  type DeleteCgtPpdOverridesServiceOutcome                      = ServiceOutcome[Unit]
  type RetrieveOtherCgtServiceOutcome                           = ServiceOutcome[RetrieveOtherCgtResponse]
  type RetrieveAllResidentialPropertyCgtServiceOutcome          = ServiceOutcome[RetrieveAllResidentialPropertyCgtResponse]

}
