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

package v1.models.request.amendSavings

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class AmendSecuritiesSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |   "taxTakenOff": 100.0,
      |   "grossAmount": 1455.0,
      |   "netAmount": 123.22
      |}
    """.stripMargin
  )

  val model: AmendSecurities =
    AmendSecurities(
      taxTakenOff = Some(100.0),
      grossAmount = 1455.0,
      netAmount = Some(123.22)
    )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |   "taxTakenOff": 100.0,
      |   "grossAmount": 1455.0,
      |   "netAmount": 123.22
      |}
    """.stripMargin
  )

  val minimumDesResponse: JsValue = Json.parse(
    """
      |{
      |   "grossAmount": 1455.0
      |}
    """.stripMargin
  )

  val minimumModel: AmendSecurities =
    AmendSecurities(
      taxTakenOff = None,
      grossAmount = 1455.0,
      netAmount = None
    )

  val desResponseInvalid: JsValue = Json.parse(
    """
      |{
      |   "taxTakenOff": "abc",
      |   "grossAmount": 1455.0,
      |   "netAmount": 123.22
      |}
    """.stripMargin
  )

  "SecuritiesItems" when {
    "read from valid JSON" should {
      "produce the expected SecuritiesItems object" in {
        desResponse.as[AmendSecurities] shouldBe model
      }
    }

    "read from a JSON with only mandatory fields" should {
      "produce the expected AmendSecurities object" in {
        minimumDesResponse.as[AmendSecurities] shouldBe minimumModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        desResponseInvalid.validate[AmendSecurities] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(model) shouldBe mtdResponse
      }
    }
  }

}
