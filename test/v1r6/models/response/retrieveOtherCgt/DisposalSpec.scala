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

package v1r6.models.response.retrieveOtherCgt

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class DisposalSpec extends UnitSpec {

  val validResponseJson: JsValue = Json.parse(
    """
      |{
      |   "assetType":"otherProperty",
      |   "assetDescription":"string",
      |   "acquisitionDate":"2021-05-07",
      |   "disposalDate":"2021-05-07",
      |   "disposalProceeds":59999999999.99,
      |   "allowableCosts":59999999999.99,
      |   "gain":59999999999.99,
      |   "claimOrElectionCodes":[
      |      "OTH"
      |   ],
      |   "gainAfterRelief":59999999999.99,
      |   "rttTaxPaid":59999999999.99
      |}
     """.stripMargin
  )

  val minimumValidResponseJson: JsValue = Json.parse(
    """
      |{
      |   "assetType":"otherProperty",
      |   "assetDescription":"string",
      |   "acquisitionDate":"2021-05-07",
      |   "disposalDate":"2021-05-07",
      |   "disposalProceeds":59999999999.99,
      |   "allowableCosts":59999999999.99
      |}
     """.stripMargin
  )

  val invalidJson: JsValue = JsObject.empty

  val responseModel: Disposal = Disposal(
    assetType = "otherProperty",
    assetDescription = "string",
    acquisitionDate = "2021-05-07",
    disposalDate = "2021-05-07",
    disposalProceeds = 59999999999.99,
    allowableCosts = 59999999999.99,
    gain = Some(59999999999.99),
    loss = None,
    claimOrElectionCodes = Some(Seq("OTH")),
    gainAfterRelief = Some(59999999999.99),
    lossAfterRelief = None,
    rttTaxPaid = Some(59999999999.99)
  )

  val minimumResponseModel: Disposal = Disposal(
    assetType = "otherProperty",
    assetDescription = "string",
    acquisitionDate = "2021-05-07",
    disposalDate = "2021-05-07",
    disposalProceeds = 59999999999.99,
    allowableCosts = 59999999999.99,
    gain = None,
    loss = None,
    claimOrElectionCodes = None,
    gainAfterRelief = None,
    lossAfterRelief = None,
    rttTaxPaid = None
  )

  "Disposal" when {
    "read from valid JSON" should {
      "produce the expected response model" in {
        validResponseJson.as[Disposal] shouldBe responseModel
      }
    }

    "read from the minimum valid JSON" should {
      "produce the expected response model" in {
        minimumValidResponseJson.as[Disposal] shouldBe minimumResponseModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        invalidJson.validate[Disposal] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(responseModel) shouldBe validResponseJson
      }
    }
  }
}
