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

package v1.models.request.amendOtherEmployment

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec

class AmendShareOptionItemSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |  "employerName": "Company Ltd",
      |  "employerRef" : "AB1321/123",
      |  "schemePlanType": "EMI",
      |  "dateOfOptionGrant": "2019-11-20",
      |  "dateOfEvent": "2019-11-20",
      |  "optionNotExercisedButConsiderationReceived": true,
      |  "amountOfConsiderationReceived": 23122.22,
      |  "noOfSharesAcquired": 1,
      |  "classOfSharesAcquired": "FIRST",
      |  "exercisePrice": 12.22,
      |  "amountPaidForOption": 123.22,
      |  "marketValueOfSharesOnExcise": 1232.22,
      |  "profitOnOptionExercised": 1232.33,
      |  "employersNicPaid": 2312.22,
      |  "taxableAmount" : 2132.22
      |}
    """.stripMargin
  )

  private val model = AmendShareOptionItem(
    employerName = ("Company Ltd"),
    employerRef = Some("AB1321/123"),
    schemePlanType = "EMI",
    dateOfOptionGrant = "2019-11-20",
    dateOfEvent = "2019-11-20",
    optionNotExercisedButConsiderationReceived = true,
    amountOfConsiderationReceived = 23122.22,
    noOfSharesAcquired = 1,
    classOfSharesAcquired = "FIRST",
    exercisePrice = 12.22,
    amountPaidForOption = 123.22,
    marketValueOfSharesOnExcise = 1232.22,
    profitOnOptionExercised = 1232.33,
    employersNicPaid = 2312.22,
    taxableAmount = 2132.22
  )

  "AmendShareOptionItem" when {
    "read from valid JSON" should {
      "produce the expected AmendShareOptionItem object" in {
        json.as[AmendShareOptionItem] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty
        invalidJson.validate[AmendShareOptionItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}
