/*
 * Copyright 2021 HM Revenue & Customs
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

package v1r7.models.request.createAmendCgtPpdOverrides

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class MultiplePropertyDisposalsSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "ppdSubmissionId": "AB0000000092",
      |   "amountOfNetGain": 1234.78
      |}
      |""".stripMargin
  )

  val multiplePropertyDisposalsModel: MultiplePropertyDisposals =
    MultiplePropertyDisposals(
      "AB0000000092",
      Some(1234.78),
      None
    )

  val desJson: JsValue = Json.parse(
    """
      |{
      |   "ppdSubmissionId": "AB0000000092",
      |   "amountOfNetGain": 1234.78
      |}
      |""".stripMargin
  )

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |   "ppdSubmissionId": 83287398.11,
      |   "amountOfNetGain": 1234.78
      |}
      |""".stripMargin
  )

  "MultiplePropertyDisposals" when {
    "read from a valid JSON" should {
      "produce the expected object" in {
        mtdJson.as[MultiplePropertyDisposals] shouldBe multiplePropertyDisposalsModel
      }
    }

    "read from an invalid json" should {
      "provide a JsError" in {
        invalidJson.validate[MultiplePropertyDisposals] shouldBe a[JsError]
      }
    }

    "written JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(multiplePropertyDisposalsModel) shouldBe desJson
      }
    }
  }
}
