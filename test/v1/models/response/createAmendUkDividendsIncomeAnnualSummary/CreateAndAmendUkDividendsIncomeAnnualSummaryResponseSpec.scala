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

package v1.models.response.createAmendUkDividendsIncomeAnnualSummary

import api.models.hateoas.Link
import api.models.hateoas.Method.{DELETE, GET, PUT}
import mocks.MockAppConfig
import support.UnitSpec

class CreateAndAmendUkDividendsIncomeAnnualSummaryResponseSpec extends UnitSpec with MockAppConfig {

  "LinksFactory" should {
    "return the correct links" in {
      val nino = "someNino"
      val taxYear = "2017-18"
      MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()
      CreateAndAmendUkDividendsIncomeAnnualSummaryHateoasData.LinksFactory.links(
        mockAppConfig,
        CreateAndAmendUkDividendsIncomeAnnualSummaryHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/individuals/income-received/uk-dividends/$nino/$taxYear", PUT, "create-and-amend-uk-dividends-income"),
          Link(s"/individuals/income-received/uk-dividends/$nino/$taxYear", GET, "self"),
          Link(s"/individuals/income-received/uk-dividends/$nino/$taxYear", DELETE, "delete-uk-dividends-income")
        )
    }

  }

}
