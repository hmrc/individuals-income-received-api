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

package v1.models.request.createAmendOtherCgt

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec
import v1.models.domain.{AssetType, ClaimOrElectionCodes}

class DisposalSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "assetType": "otherProperty",
      |  "assetDescription": "Property Sale",
      |  "acquisitionDate": "2021-01-01",
      |  "disposalDate": "2021-02-01",
      |  "disposalProceeds": 1000.12,
      |  "allowableCosts": 100.13,
      |  "gain": 900.12,
      |  "claimOrElectionCodes": [
      |    "PRR"
      |  ],
      |  "gainAfterRelief": 10.12,
      |  "rttTaxPaid": 12.12
      |}
      |""".stripMargin
  )

  val mtdJsonWithMultipleCodes: JsValue = Json.parse(
    """
      |{
      |  "assetType": "otherProperty",
      |  "assetDescription": "Property Sale",
      |  "acquisitionDate": "2021-01-01",
      |  "disposalDate": "2021-02-01",
      |  "disposalProceeds": 1000.12,
      |  "allowableCosts": 100.13,
      |  "gain": 900.12,
      |  "claimOrElectionCodes": [
      |    "PRR",
      |    "OTH",
      |    "BAD"
      |  ],
      |  "gainAfterRelief": 10.12,
      |  "rttTaxPaid": 12.12
      |}
      |""".stripMargin
  )

  val mtdRequestBody: Disposal = Disposal(
    AssetType.otherProperty.toString,
    "Property Sale",
    "2021-01-01",
    "2021-02-01",
    1000.12,
    100.13,
    Some(900.12),
    None,
    Some(Seq(ClaimOrElectionCodes.PRR.toString)),
    Some(10.12),
    None,
    Some(12.12)
  )

  val mtdRequestBodyWithMultipleCodes: Disposal = Disposal(
    AssetType.otherProperty.toString,
    "Property Sale",
    "2021-01-01",
    "2021-02-01",
    1000.12,
    100.13,
    Some(900.12),
    None,
    Some(Seq(ClaimOrElectionCodes.PRR.toString, ClaimOrElectionCodes.OTH.toString, ClaimOrElectionCodes.BAD.toString)),
    Some(10.12),
    None,
    Some(12.12)
  )

  val desJson: JsValue = Json.parse(
    """
      |{
      |  "assetType":"otherProperty",
      |  "assetDescription":"Property Sale",
      |  "acquisitionDate":"2021-01-01",
      |  "disposalDate":"2021-02-01",
      |  "disposalProceeds":1000.12,
      |  "allowableCosts":100.13,
      |  "gain":900.12,
      |  "claimOrElectionCodes":[
      |    "PRR"
      |  ],
      |  "gainAfterRelief":10.12,
      |  "rttTaxPaid":12.12
      |}
      |""".stripMargin
  )

  val desJsonWithMultipleCodes: JsValue = Json.parse(
    """
      |{
      |  "assetType":"otherProperty",
      |  "assetDescription":"Property Sale",
      |  "acquisitionDate":"2021-01-01",
      |  "disposalDate":"2021-02-01",
      |  "disposalProceeds":1000.12,
      |  "allowableCosts":100.13,
      |  "gain":900.12,
      |  "claimOrElectionCodes":[
      |    "PRR",
      |    "OTH",
      |    "BAD"
      |  ],
      |  "gainAfterRelief":10.12,
      |  "rttTaxPaid":12.12
      |}
      |""".stripMargin
  )

  val emptyJson: JsValue = JsObject.empty

  val invalidJson: JsValue = Json.parse(
    """
      |{
      |
      |}
      |""".stripMargin
  )

  "Disposals" when {
    "read from a valid JSON" should {
      "produce the expected object" in {
        mtdJson.as[Disposal] shouldBe mtdRequestBody
      }
    }

    "read from a valid JSON with multiple codes" should {
      "produce the expected object" in {
        mtdJsonWithMultipleCodes.as[Disposal] shouldBe mtdRequestBodyWithMultipleCodes
      }
    }

    "read from invalid Json" should {
      "provide a JsError" in {
        invalidJson.validate[Disposal] shouldBe a[JsError]
      }
    }

    "written JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(mtdRequestBody) shouldBe desJson
      }
    }

    "written JSON with multiple codes" should {
      "produce the expected JsObject" in {
        Json.toJson(mtdRequestBodyWithMultipleCodes) shouldBe desJsonWithMultipleCodes
      }
    }
  }
}
