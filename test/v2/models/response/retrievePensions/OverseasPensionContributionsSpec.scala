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

package v2.models.response.retrievePensions

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class OverseasPensionContributionsSpec extends UnitSpec {

  private val mtdJson = Json.parse(
    """
      |{
      |   "customerReference": "PENSIONINCOME245",
      |   "exemptEmployersPensionContribs": 200.23,
      |   "migrantMemReliefQopsRefNo": "QOPS000000",
      |   "dblTaxationRelief": 4.23,
      |   "dblTaxationCountryCode": "FRA",
      |   "dblTaxationArticle": "AB3211-1",
      |   "dblTaxationTreaty": "Treaty",
      |   "sf74reference": "SF74-123456"
      |}
    """.stripMargin
  )

  private val desJson = Json.parse(
    """
      |{
      |   "customerReference": "PENSIONINCOME245",
      |   "exemptEmployersPensionContribs": 200.23,
      |   "migrantMemReliefQopsRefNo": "QOPS000000",
      |   "dblTaxationRelief": 4.23,
      |   "dblTaxationCountry": "FRA",
      |   "dblTaxationArticle": "AB3211-1",
      |   "dblTaxationTreaty": "Treaty",
      |   "sf74Reference": "SF74-123456"
      |}
    """.stripMargin
  )

  private val model = OverseasPensionContributions(
    customerReference = Some("PENSIONINCOME245"),
    exemptEmployersPensionContribs = 200.23,
    migrantMemReliefQopsRefNo = Some("QOPS000000"),
    dblTaxationRelief = Some(4.23),
    dblTaxationCountryCode = Some("FRA"),
    dblTaxationArticle = Some("AB3211-1"),
    dblTaxationTreaty = Some("Treaty"),
    sf74reference = Some("SF74-123456")
  )

  "OverseasPensionContributionsItem" when {
    "read from valid JSON" should {
      "produce the expected OverseasPensionContributionsItem object" in {
        desJson.as[OverseasPensionContributions] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty

        invalidJson.validate[OverseasPensionContributions] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

}
