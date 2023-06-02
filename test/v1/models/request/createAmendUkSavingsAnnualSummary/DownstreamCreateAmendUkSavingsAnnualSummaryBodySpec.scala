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

package v1.models.request.createAmendUkSavingsAnnualSummary

import api.models.domain.{Nino, TaxYear}
import play.api.libs.json.Json
import support.UnitSpec

class DownstreamCreateAmendUkSavingsAnnualSummaryBodySpec extends UnitSpec {

  "DownstreamCreateAmendUkSavingsAnnualSummaryBody" when {
    "written to JSON" must {
      "produce expected downstream request body" in {
        Json.toJson(
          DownstreamCreateAmendUkSavingsAnnualSummaryBody(
            incomeSourceId = "someIncomeSourceId",
            taxedUkInterest = Some(10.12),
            untaxedUkInterest = Some(11.12))) shouldBe
          Json.parse("""{
              |  "incomeSourceId": "someIncomeSourceId",
              |  "taxedUkInterest": 10.12,
              |  "untaxedUkInterest": 11.12
              |}""".stripMargin)
      }
    }

    "created from an MTD request" must {
      "work" in {
        DownstreamCreateAmendUkSavingsAnnualSummaryBody(
          CreateAmendUkSavingsAnnualSummaryRequest(
            Nino("AA121212A"),
            TaxYear.fromMtd("2020-21"),
            savingsAccountId = "someSavingsAccountId",
            body = CreateAmendUkSavingsAnnualSummaryBody(taxedUkInterest = Some(10.12), untaxedUkInterest = Some(11.12))
          )) shouldBe
          DownstreamCreateAmendUkSavingsAnnualSummaryBody(
            incomeSourceId = "someSavingsAccountId",
            taxedUkInterest = Some(10.12),
            untaxedUkInterest = Some(11.12))
      }
    }
  }

}
