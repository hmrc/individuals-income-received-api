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

package v1.models.response.retrieveUkDividendsAnnualIncomeSummary

import api.models.hateoas.Link
import api.models.hateoas.Method.{DELETE, GET, PUT}
import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec

class RetrieveUkDividendsAnnualIncomeSummaryResponseSpec extends UnitSpec with MockAppConfig with RetrieveUkDividendsAnnualIncomeSummaryFixture {

  "RetrieveUkDividendsAnnualIncomeSummaryResponse" must {

    "read from downstream JSON" in {
      downstreamResponseJson.as[RetrieveUkDividendsAnnualIncomeSummaryResponse] shouldBe responseModel
    }

    "write to MTD JSON" in {
      Json.toJson(responseModel) shouldBe mtdResponseJson
    }
  }

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "mytaxyear"
      val context = "individuals/income-received"

      MockedAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes

      RetrieveUkDividendsAnnualIncomeSummaryResponse.LinksFactory.links(
        mockAppConfig,
        RetrieveUkDividendsAnnualIncomeSummaryHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/$context/uk-dividends/$nino/$taxYear", PUT, "create-and-amend-uk-dividends-income"),
          Link(s"/$context/uk-dividends/$nino/$taxYear", GET, "self"),
          Link(s"/$context/uk-dividends/$nino/$taxYear", DELETE, "delete-uk-dividends-income")
        )
    }
  }

}
