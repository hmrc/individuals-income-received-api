/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.createAmendUkSavingsIncomeAnnualSummary

import api.models.hateoas.Link
import api.models.hateoas.Method.{DELETE, GET, PUT}
import mocks.MockAppConfig
import support.UnitSpec

class CreateAndAmendUkSavingsAnnualSummaryHateoasDataSpec extends UnitSpec with MockAppConfig {

  "LinksFactory" should {
    "return the correct links" in {
      val nino             = "someNino"
      val taxYear          = "2017-18"
      val savingsAccountId = "someAcctId"
      val context          = "some/context"

      MockedAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes

      CreateAndAmendUkSavingsAnnualSummaryHateoasData.LinksFactory.links(
        mockAppConfig,
        CreateAndAmendUkSavingsAnnualSummaryHateoasData(nino, taxYear, savingsAccountId)) shouldBe
        Seq(
          Link(s"/$context/savings/uk-accounts/$nino/$taxYear/$savingsAccountId", PUT, "create-and-amend-uk-savings-account-annual-summary"),
          Link(s"/$context/savings/uk-accounts/$nino/$taxYear/$savingsAccountId", GET, "self"),
          Link(s"/$context/savings/uk-accounts/$nino/$taxYear/$savingsAccountId", DELETE, "delete-uk-savings-account-annual-summary")
        )
    }

  }

}
