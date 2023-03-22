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
import v1.models.response.retrieveAllResidentialPropertyCgt.RetrieveAllResidentialPropertyCgtResponse
import v1.models.response.retrieveDividends.RetrieveDividendsResponse
import v1.models.response.retrieveOtherCgt.RetrieveOtherCgtResponse

package object services {

  type ServiceOutcome[A] = Either[ErrorWrapper, ResponseWrapper[A]]

  // Dividends income
  type AmendDividendsServiceOutcome    = ServiceOutcome[Unit]
  type DeleteDividendsServiceOutcome   = ServiceOutcome[Unit]
  type RetrieveDividendsServiceOutcome = ServiceOutcome[RetrieveDividendsResponse]

  // Capital Tax Gains
  type CreateAmendCgtPpdOverridesServiceOutcome                 = ServiceOutcome[Unit]
  type CreateAmendOtherCgtServiceOutcome                        = ServiceOutcome[Unit]
  type CreateAmendCgtResidentialPropertyDisposalsServiceOutcome = ServiceOutcome[Unit]
  type DeleteCgtNonPpdServiceOutcome                            = ServiceOutcome[Unit]
  type DeleteOtherCgtServiceOutcome                             = ServiceOutcome[Unit]
  type DeleteCgtPpdOverridesServiceOutcome                      = ServiceOutcome[Unit]
  type RetrieveOtherCgtServiceOutcome                           = ServiceOutcome[RetrieveOtherCgtResponse]
  type RetrieveAllResidentialPropertyCgtServiceOutcome          = ServiceOutcome[RetrieveAllResidentialPropertyCgtResponse]

}
