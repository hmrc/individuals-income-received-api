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

class AmendSharesAwardedOrReceivedItemSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "employerName": "Company Ltd",
      |   "employerRef" : "AB1321/123",
      |   "schemePlanType": "SIP",
      |   "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |   "noOfShareSecuritiesAwarded": 11,
      |   "classOfShareAwarded": "FIRST",
      |   "dateSharesAwarded" : "2019-11-20",
      |   "sharesSubjectToRestrictions": true,
      |   "electionEnteredIgnoreRestrictions": false,
      |   "actualMarketValueOfSharesOnAward": 2123.22,
      |   "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |   "amountPaidForSharesOnAward": 123.22,
      |   "marketValueAfterRestrictionsLifted": 1232.22,
      |   "taxableAmount": 12321.22
      |}
    """.stripMargin
  )

  private val model = AmendSharesAwardedOrReceivedItem(
    employerName = "Company Ltd",
    employerRef = Some("AB1321/123"),
    schemePlanType = "SIP",
    dateSharesCeasedToBeSubjectToPlan = "2019-11-10",
    noOfShareSecuritiesAwarded = 11,
    classOfShareAwarded = "FIRST",
    dateSharesAwarded = "2019-11-20",
    sharesSubjectToRestrictions = true,
    electionEnteredIgnoreRestrictions = false,
    actualMarketValueOfSharesOnAward = 2123.22,
    unrestrictedMarketValueOfSharesOnAward = 123.22,
    amountPaidForSharesOnAward = 123.22,
    marketValueAfterRestrictionsLifted = 1232.22,
    taxableAmount = 12321.22
  )

  "AmendSharesAwardedOrReceivedItem" when {
    "read from valid JSON" should {
      "produce the expected AmendSharesAwardedOrReceivedItem object" in {
        json.as[AmendSharesAwardedOrReceivedItem] shouldBe model
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty
        invalidJson.validate[AmendSharesAwardedOrReceivedItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}
