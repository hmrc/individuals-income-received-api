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
import v1.models.response.listEmployment.{Employment, ListEmploymentResponse}
import v1.models.response.listUkSavingsAccounts.{ListUkSavingsAccountsResponse, UkSavingsAccount}
import v1.models.response.retrieveEmployment.RetrieveEmploymentResponse
import v1.models.response.retrieveFinancialDetails.RetrieveEmploymentAndFinancialDetailsResponse
import v1.models.response.retrieveUkSavingsAnnualSummary.RetrieveUkSavingsAnnualSummaryResponse

package object services {

  type ServiceOutcome[A] = Either[ErrorWrapper, ResponseWrapper[A]]

  type AddCustomEmploymentServiceOutcome                   = ServiceOutcome[AddCustomEmploymentResponse]
  type AmendCustomEmploymentServiceOutcome                 = ServiceOutcome[Unit]
  type AmendFinancialDetailsServiceOutcome                 = ServiceOutcome[Unit]
  type CreateAmendUkSavingsAnnualSummaryServiceOutcome     = ServiceOutcome[Unit]
  type DeleteCustomEmploymentServiceOutcome                = ServiceOutcome[Unit]
  type DeleteEmploymentFinancialDetailsServiceOutcome      = ServiceOutcome[Unit]
  type ListEmploymentsServiceOutcome                       = ServiceOutcome[ListEmploymentResponse[Employment]]
  type ListUkSavingsAccountsServiceOutcome                 = ServiceOutcome[ListUkSavingsAccountsResponse[UkSavingsAccount]]
  type RetrieveEmploymentAndFinancialDetailsServiceOutcome = ServiceOutcome[RetrieveEmploymentAndFinancialDetailsResponse]
  type RetrieveEmploymentServiceOutcome                    = ServiceOutcome[RetrieveEmploymentResponse]
  type RetrieveUkSavingsAccountAnnualSummaryServiceOutcome = ServiceOutcome[RetrieveUkSavingsAnnualSummaryResponse]

}
