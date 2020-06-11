/*
 * Copyright 2020 HM Revenue & Customs
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

package utils

import play.api.libs.json.{JsArray, JsNull, JsString, JsValue, Json, OWrites, Reads, __}
import support.UnitSpec

class JsonUtilsSpec extends UnitSpec with JsonUtils {

  case class TestClass(field1: String, field2: Option[String])

  object TestClass extends JsonUtils {
    implicit val reads: Reads[TestClass] = Json.reads[TestClass].removeField("field2")
    implicit val wrongReads: Reads[TestClass] = Json.reads[TestClass].removeField("field3")
    implicit val writes: OWrites[TestClass] = Json.writes[TestClass].removeField("field2")
    implicit val wrongWrites: OWrites[TestClass] = Json.writes[TestClass].removeField("field3")
  }

  private val testData: TestClass = TestClass("value1", Some("value2"))

  private val removedFieldJson: JsValue = Json.parse(
    """
      |{
      |   "field1": "value1"
      |}
    """.stripMargin
  )

  private val fullJson: JsValue = Json.parse(
    """
      |{
      |   "field1": "value1",
      |   "field2": "value2"
      |}
    """.stripMargin
  )

  "JsonUtils" when {
    "removeField (writes)" should {
      "remove that field if it is present" in {
        Json.toJson(testData)(TestClass.writes) shouldBe removedFieldJson
      }

      "do nothing if the specified field does not exist" in {
        Json.toJson(testData)(TestClass.wrongWrites) shouldBe fullJson
      }
    }

    "removeField (reads)" should {
      "remove that field if it is present" in {
        fullJson.as[TestClass](TestClass.reads) shouldBe TestClass("value1", None)
      }

      "do nothing if the specified field does not exist" in {
        fullJson.as[TestClass](TestClass.wrongReads) shouldBe testData
      }
    }

    "mapEmptySeqToNone" must {
      val reads = __.readNullable[Seq[String]].mapEmptySeqToNone

      "map non-empty sequence to Some(non-empty sequence)" in {
        JsArray(Seq(JsString("value0"), JsString("value1"))).as(reads) shouldBe Some(Seq("value0", "value1"))
      }

      "map empty sequence to None" in {
        JsArray.empty.as(reads) shouldBe None
      }

      "map None to None" in {
        JsNull.as(reads) shouldBe None
      }
    }
  }
}
