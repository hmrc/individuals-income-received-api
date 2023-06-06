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

import play.api.libs.json.Json
import support.UnitSpec

class DownstreamUkSavingsAnnualIncomeResponseSpec extends UnitSpec {

  "Reads" must {
    "read from downstream JSON" in {
      Json
        .parse("""{
                   |  "savingsInterestAnnualIncome": [
                   |    {
                   |      "incomeSourceId": "id1",
                   |      "taxedUkInterest": 1.12,
                   |      "untaxedUkInterest": 2.12
                   |    },
                   |    {
                   |      "incomeSourceId": "id2",
                   |      "taxedUkInterest": 3.12,
                   |      "untaxedUkInterest": 4.12
                   |    }
                   |  ]
                   |}
          |""".stripMargin)
        .as[DownstreamUkSavingsAnnualIncomeResponse] shouldBe
        DownstreamUkSavingsAnnualIncomeResponse(
          Seq(
            DownstreamUkSavingsAnnualIncomeItem(incomeSourceId = "id1", taxedUkInterest = Some(1.12), untaxedUkInterest = Some(2.12)),
            DownstreamUkSavingsAnnualIncomeItem(incomeSourceId = "id2", taxedUkInterest = Some(3.12), untaxedUkInterest = Some(4.12))
          ))
    }
  }

}
