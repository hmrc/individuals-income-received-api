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

package v1.models.response.retrieveOtherEmployment

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec
import v1.models.domain.{ShareOptionSchemeType, SharesAwardedOrReceivedSchemeType}

class RetrieveOtherEmploymentResponseSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "shareOption": [
      |      {
      |         "employerName": "Company Ltd",
      |         "employerRef" : "123/AB456",
      |         "schemePlanType": "EMI",
      |         "dateOfOptionGrant": "2019-11-20",
      |         "dateOfEvent": "2019-12-22",
      |         "optionNotExercisedButConsiderationReceived": true,
      |         "amountOfConsiderationReceived": 23122.22,
      |         "noOfSharesAcquired": 1,
      |         "classOfSharesAcquired": "FIRST",
      |         "exercisePrice": 12.22,
      |         "amountPaidForOption": 123.22,
      |         "marketValueOfSharesOnExcise": 1232.22,
      |         "profitOnOptionExercised": 1232.33,
      |         "employersNicPaid": 2312.22,
      |         "taxableAmount" : 2132.22
      |      },
      |      {
      |         "employerName": "Corp Ltd",
      |         "employerRef" : "345/AB678",
      |         "schemePlanType": "CSOP",
      |         "dateOfOptionGrant": "2019-09-10",
      |         "dateOfEvent": "2019-07-29",
      |         "optionNotExercisedButConsiderationReceived": false,
      |         "amountOfConsiderationReceived": 30000.22,
      |         "noOfSharesAcquired": 5,
      |         "classOfSharesAcquired": "SECOND",
      |         "exercisePrice": 10.50,
      |         "amountPaidForOption": 150.50,
      |         "marketValueOfSharesOnExcise": 2250.22,
      |         "profitOnOptionExercised": 1350.40,
      |         "employersNicPaid": 2450.25,
      |         "taxableAmount" : 2140.20
      |      }
      |   ],
      |   "sharesAwardedOrReceived": [
      |      {
      |         "employerName": "Company Ltd",
      |         "employerRef" : "123/AB456",
      |         "schemePlanType": "SIP",
      |         "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |         "noOfShareSecuritiesAwarded": 11,
      |         "classOfShareAwarded": "FIRST",
      |         "dateSharesAwarded" : "2019-12-20",
      |         "sharesSubjectToRestrictions": true,
      |         "electionEnteredIgnoreRestrictions": false,
      |         "actualMarketValueOfSharesOnAward": 2123.22,
      |         "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |         "amountPaidForSharesOnAward": 123.22,
      |         "marketValueAfterRestrictionsLifted": 1232.22,
      |         "taxableAmount": 12321.22
      |      },
      |      {
      |         "employerName": "Corp Ltd",
      |         "employerRef" : "345/AB678",
      |         "schemePlanType": "Other",
      |         "dateSharesCeasedToBeSubjectToPlan": "2019-10-25",
      |         "noOfShareSecuritiesAwarded": 15,
      |         "classOfShareAwarded": "SECOND",
      |         "dateSharesAwarded" : "2019-08-23",
      |         "sharesSubjectToRestrictions": true,
      |         "electionEnteredIgnoreRestrictions": false,
      |         "actualMarketValueOfSharesOnAward": 2140.23,
      |         "unrestrictedMarketValueOfSharesOnAward": 125.30,
      |         "amountPaidForSharesOnAward": 124.25,
      |         "marketValueAfterRestrictionsLifted": 1259.40,
      |         "taxableAmount": 12450.30
      |      }
      |   ],
      |   "disability": {
      |         "customerReference": "customer reference",
      |         "amountDeducted": 1223.22
      |   },
      |   "foreignService": {
      |         "customerReference": "cust ref",
      |         "amountDeducted": 1234.50
      |   }
      |}
    """.stripMargin
  )

  private val shareOptionItemModel = Seq(
    ShareOptionItem(
      employerName = "Company Ltd",
      employerRef = Some("123/AB456"),
      schemePlanType = ShareOptionSchemeType.EMI,
      dateOfOptionGrant = "2019-11-20",
      dateOfEvent = "2019-12-22",
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
    ),
    ShareOptionItem(
      employerName = "Corp Ltd",
      employerRef = Some("345/AB678"),
      schemePlanType = ShareOptionSchemeType.CSOP,
      dateOfOptionGrant = "2019-09-10",
      dateOfEvent = "2019-07-29",
      optionNotExercisedButConsiderationReceived = false,
      amountOfConsiderationReceived = 30000.22,
      noOfSharesAcquired = 5,
      classOfSharesAcquired = "SECOND",
      exercisePrice = 10.50,
      amountPaidForOption = 150.50,
      marketValueOfSharesOnExcise = 2250.22,
      profitOnOptionExercised = 1350.40,
      employersNicPaid = 2450.25,
      taxableAmount = 2140.20
    )
  )

  private val sharesAwardedOrReceivedItemModel = Seq(
    SharesAwardedOrReceivedItem(
      employerName = "Company Ltd",
      employerRef = Some("123/AB456"),
      schemePlanType = SharesAwardedOrReceivedSchemeType.SIP,
      dateSharesCeasedToBeSubjectToPlan = "2019-11-10",
      noOfShareSecuritiesAwarded = 11,
      classOfShareAwarded = "FIRST",
      dateSharesAwarded = "2019-12-20",
      sharesSubjectToRestrictions = true,
      electionEnteredIgnoreRestrictions = false,
      actualMarketValueOfSharesOnAward = 2123.22,
      unrestrictedMarketValueOfSharesOnAward = 123.22,
      amountPaidForSharesOnAward = 123.22,
      marketValueAfterRestrictionsLifted = 1232.22,
      taxableAmount = 12321.22
    ),
    SharesAwardedOrReceivedItem(
      employerName = "Corp Ltd",
      employerRef = Some("345/AB678"),
      schemePlanType = SharesAwardedOrReceivedSchemeType.Other,
      dateSharesCeasedToBeSubjectToPlan = "2019-10-25",
      noOfShareSecuritiesAwarded = 15,
      classOfShareAwarded = "SECOND",
      dateSharesAwarded = "2019-08-23",
      sharesSubjectToRestrictions = true,
      electionEnteredIgnoreRestrictions = false,
      actualMarketValueOfSharesOnAward = 2140.23,
      unrestrictedMarketValueOfSharesOnAward = 125.30,
      amountPaidForSharesOnAward = 124.25,
      marketValueAfterRestrictionsLifted = 1259.40,
      taxableAmount = 12450.30
    )
  )

  private val disabilityModel = CommonOtherEmployment(
    customerReference = Some("customer reference"),
    amountDeducted = 1223.22
  )

  private val foreignServiceModel = CommonOtherEmployment(
    customerReference = Some("cust ref"),
    amountDeducted = 1234.50
  )

  private val responseModel = RetrieveOtherEmploymentResponse(
    Some(shareOptionItemModel),
    Some(sharesAwardedOrReceivedItemModel),
    Some(disabilityModel),
    Some(foreignServiceModel)
  )

  "RetrieveOtherEmploymentResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveOtherEmploymentResponse object" in {
        json.as[RetrieveOtherEmploymentResponse] shouldBe responseModel
      }
    }

    "read from valid JSON with empty shareOption and sharesAwardedOrReceived arrays" should {
      "produce an empty RetrieveOtherResponse object" in {
        val json = Json.parse(
          """
            |{
            |   "shareOption": [ ],
            |   "sharesAwardedOrReceived": [ ]
            |}
          """.stripMargin
        )

        json.as[RetrieveOtherEmploymentResponse] shouldBe RetrieveOtherEmploymentResponse.empty
      }
    }

    "read from empty JSON" should {
      "produce an empty RetrieveOtherEmploymentResponse object" in {
        val emptyJson = JsObject.empty

        emptyJson.as[RetrieveOtherEmploymentResponse] shouldBe RetrieveOtherEmploymentResponse.empty
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |   "disability": {
            |        "customerReference": "customer reference",
            |        "amountDeducted": "no"
            |   }
            |}
          """.stripMargin
        )

        invalidJson.validate[RetrieveOtherEmploymentResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(responseModel) shouldBe json
      }
    }
  }
}
