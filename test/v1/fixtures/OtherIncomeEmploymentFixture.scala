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

package v1.fixtures

import api.models.domain.{ShareOptionSchemeType, SharesAwardedOrReceivedSchemeType, Timestamp}
import v1.models.response.retrieveOtherEmployment._

object OtherIncomeEmploymentFixture {

  val shareOption: ShareOptionItem = ShareOptionItem(
    employerName = "Company Ltd",
    employerRef = Some("123/AB456"),
    schemePlanType = ShareOptionSchemeType.EMI,
    dateOfOptionGrant = "2019-11-20",
    dateOfEvent = "2019-12-22",
    optionNotExercisedButConsiderationReceived = true,
    amountOfConsiderationReceived = 23122.22,
    noOfSharesAcquired = 1,
    classOfSharesAcquired = "FIRST",
    exercisePrice = 12.22,
    amountPaidForOption = 123.22,
    marketValueOfSharesOnExcise = 1232.22,
    profitOnOptionExercised = 1232.33,
    employersNicPaid = 2312.22,
    taxableAmount = 2132.22
  )

  val sharesAwardedOrReceived: SharesAwardedOrReceivedItem = SharesAwardedOrReceivedItem(
    employerName = "Company Ltd",
    employerRef = Some("123/AB456"),
    schemePlanType = SharesAwardedOrReceivedSchemeType.SIP,
    dateSharesCeasedToBeSubjectToPlan = "2019-11-10",
    noOfShareSecuritiesAwarded = 11,
    classOfShareAwarded = "FIRST",
    dateSharesAwarded = "2019-12-20",
    sharesSubjectToRestrictions = true,
    electionEnteredIgnoreRestrictions = false,
    actualMarketValueOfSharesOnAward = 2123.22,
    unrestrictedMarketValueOfSharesOnAward = 123.22,
    amountPaidForSharesOnAward = 123.22,
    marketValueAfterRestrictionsLifted = 1232.22,
    taxableAmount = 12321.22
  )

  val commonOtherEmployment: CommonOtherEmployment = CommonOtherEmployment(
    customerReference = Some("customer reference"),
    amountDeducted = 1223.22
  )

  val benefitFromEmployerFinancedRetirementScheme: BenefitFromEmployerFinancedRetirementSchemeItem = BenefitFromEmployerFinancedRetirementSchemeItem(
    amount = 5000.99,
    exemptAmount = Some(2345.99),
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  val taxableLumpSumsAndCertainIncome: TaxableLumpSumsAndCertainIncomeItem = TaxableLumpSumsAndCertainIncomeItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  val redundancyCompensationPaymentsOverExemption: RedundancyCompensationPaymentsOverExemptionItem = RedundancyCompensationPaymentsOverExemptionItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  val redundancyCompensationPaymentsUnderExemption: RedundancyCompensationPaymentsUnderExemptionItem =
    RedundancyCompensationPaymentsUnderExemptionItem(
      amount = Some(5000.99)
    )

  val lumpSums: LumpSums = LumpSums(
    employerName = "BPDTS Ltd",
    employerRef = "123/AB456",
    taxableLumpSumsAndCertainIncome = Some(taxableLumpSumsAndCertainIncome),
    benefitFromEmployerFinancedRetirementScheme = Some(benefitFromEmployerFinancedRetirementScheme),
    redundancyCompensationPaymentsOverExemption = Some(redundancyCompensationPaymentsOverExemption),
    redundancyCompensationPaymentsUnderExemption = Some(redundancyCompensationPaymentsUnderExemption)
  )

  val retrieveResponse: RetrieveOtherEmploymentResponse = RetrieveOtherEmploymentResponse(
    submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
    shareOption = Some(Seq(shareOption)),
    sharesAwardedOrReceived = Some(Seq(sharesAwardedOrReceived)),
    disability = Some(commonOtherEmployment),
    foreignService = Some(commonOtherEmployment),
    lumpSums = Some(Seq(lumpSums))
  )

  val retrieveOtherResponseModel: RetrieveOtherEmploymentResponse = RetrieveOtherEmploymentResponse(
    submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
    shareOption = Some(Seq(shareOption)),
    sharesAwardedOrReceived = Some(Seq(sharesAwardedOrReceived)),
    disability = Some(commonOtherEmployment),
    foreignService = Some(commonOtherEmployment),
    lumpSums = Some(Seq(lumpSums))
  )

}
