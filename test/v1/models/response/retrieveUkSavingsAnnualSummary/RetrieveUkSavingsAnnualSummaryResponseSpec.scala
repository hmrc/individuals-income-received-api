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

package v1.models.response.retrieveUkSavingsAnnualSummary

import api.models.hateoas.Link
import api.models.hateoas.Method._
import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec

class RetrieveUkSavingsAnnualSummaryResponseSpec extends UnitSpec with MockAppConfig {

  "writes" must {
    "write as MTD JSON" in {
      Json.toJson(RetrieveUkSavingsAnnualSummaryResponse(taxedUkInterest = Some(1.12), untaxedUkInterest = Some(2.12))) shouldBe
        Json.parse(
          """{
            |  "taxedUkInterest": 1.12,
            |  "untaxedUkInterest": 2.12
            |}""".stripMargin)
    }
  }

  "LinksFactory" should {
    "return the correct links" in {
      val nino = "mynino"
      val taxYear = "mytaxyear"
      val context = "individuals/income-received"
      val accountId = "someAccount"

      MockedAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes()
      RetrieveUkSavingsAnnualSummaryResponse.LinksFactory.links(
        mockAppConfig,
        RetrieveUkSavingsAnnualSummaryResponseHateoasData(nino, taxYear, accountId)) shouldBe
        Seq(
          Link(s"/$context/savings/uk-accounts/$nino/$taxYear/$accountId", PUT, "create-and-amend-uk-savings-account-annual-summary"),
          Link(s"/$context/savings/uk-accounts/$nino/$taxYear/$accountId", GET, "self")
        )
    }
  }

}
