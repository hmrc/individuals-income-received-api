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

package v1.models.request.createAmendUkDividendsIncomeAnnualSummary

import play.api.libs.json.{JsObject, JsValue, Json}
import support.UnitSpec

class CreateAmendUkDividendsIncomeAnnualSummaryBodySpec extends UnitSpec {

  private val json: JsValue = Json.parse(
    """
      |{
      |   "ukDividends": 1000.99,
      |   "otherUkDividends": 1001.99
      |}
      |""".stripMargin
  )

  private val model: CreateAmendUkDividendsIncomeAnnualSummaryBody = CreateAmendUkDividendsIncomeAnnualSummaryBody(
    Some(1000.99),
    Some(1001.99)
  )

  val emptyJson: JsValue = JsObject.empty

  private val emptyModel: CreateAmendUkDividendsIncomeAnnualSummaryBody = CreateAmendUkDividendsIncomeAnnualSummaryBody(
    None,
    None
  )

  "CreateAmendUkDividendsIncomeSummaryBody" when {
    "reads" should {
      "turn JSON into a model" in {
        json.as[CreateAmendUkDividendsIncomeAnnualSummaryBody] shouldBe model
      }
    }

    "read from an empty JSON" should {
      "produce an empty object" in {
        emptyJson.as[CreateAmendUkDividendsIncomeAnnualSummaryBody] shouldBe emptyModel
      }
    }

    "writes" should {
      "turn a model into JSON" in {
        Json.toJson(model) shouldBe json
      }
    }

    "written from an empty object" should {
      "produce an empty JSON" in {
        Json.toJson(emptyModel) shouldBe emptyJson
      }
    }

    "treat all fields as optional" in {
      JsObject.empty.as[CreateAmendUkDividendsIncomeAnnualSummaryBody] shouldBe
        CreateAmendUkDividendsIncomeAnnualSummaryBody(
          ukDividends = None,
          otherUkDividends = None
        )
    }
  }

}
