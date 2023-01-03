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

import play.api.libs.json.{JsObject, JsValue, Json}
import support.UnitSpec

class CreateAmendUkSavingsAnnualSummaryBodySpec extends UnitSpec {

  private val json: JsValue = Json.parse(
    """
      |{
      |   "taxedUkInterest": 1000.99,
      |   "untaxedUkInterest": 1001.99
      |}
      |""".stripMargin
  )

  private val model: CreateAmendUkSavingsAnnualSummaryBody = CreateAmendUkSavingsAnnualSummaryBody(
    Some(1000.99),
    Some(1001.99)
  )

  val emptyJson: JsValue = JsObject.empty

  private val emptyModel: CreateAmendUkSavingsAnnualSummaryBody = CreateAmendUkSavingsAnnualSummaryBody(
    None,
    None
  )

  "CreateAmendUkSavingsAnnualSummaryBody reads" when {
    "reading valid JSON" should {
      "read OK" in {
        json.as[CreateAmendUkSavingsAnnualSummaryBody] shouldBe model
      }
    }

    "reading empty JSON" should {
      "create an empty instance" in {
        emptyJson.as[CreateAmendUkSavingsAnnualSummaryBody] shouldBe emptyModel
      }
    }
  }

  "CreateAmendUkSavingsAnnualSummaryBody writes" when {

    "writing a populated instance" should {
      "produce valid JSON" in {
        Json.toJson(model) shouldBe json
      }
    }

    "writing an empty object" should {
      "produce an empty JSON document" in {
        Json.toJson(emptyModel) shouldBe emptyJson
      }
    }

  }

}
