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

class AmendOtherEmploymentRequestBodySpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |  "shareOption": [
      |      {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "AB1321/123",
      |        "schemePlanType": "EMI",
      |        "dateOfOptionGrant": "2019-11-20",
      |        "dateOfEvent": "2019-11-20",
      |        "optionNotExercisedButConsiderationReceived": true,
      |        "amountOfConsiderationReceived": 23122.22,
      |        "noOfSharesAcquired": 1,
      |        "classOfSharesAcquired": "FIRST",
      |        "exercisePrice": 12.22,
      |        "amountPaidForOption": 123.22,
      |        "marketValueOfSharesOnExcise": 1232.22,
      |        "profitOnOptionExercised": 1232.33,
      |        "employersNicPaid": 2312.22,
      |        "taxableAmount" : 2132.22
      |      }
      |  ],
      |  "sharesAwardedOrReceived": [
      |       {
      |        "employerName": "Company Ltd",
      |        "employerRef" : "AB1321/123",
      |        "schemePlanType": "SIP",
      |        "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |        "noOfShareSecuritiesAwarded": 11,
      |        "classOfShareAwarded": "FIRST",
      |        "dateSharesAwarded" : "2019-11-20",
      |        "sharesSubjectToRestrictions": true,
      |        "electionEnteredIgnoreRestrictions": false,
      |        "actualMarketValueOfSharesOnAward": 2123.22,
      |        "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |        "amountPaidForSharesOnAward": 123.22,
      |        "marketValueAfterRestrictionsLifted": 1232.22,
      |        "taxableAmount": 12321.22
      |       }
      |  ],
      |  "disability":
      |    {
      |      "customerReference": "cust ref",
      |      "amountDeducted": 1223.22
      |    },
      |  "foreignService":
      |    {
      |      "customerReference": "cust ref",
      |      "amountDeducted": 1223.22
      |    },
      |  "lumpSums": [
      |    {
      |      "employerName": "BPDTS Ltd",
      |      "employerRef": "123/AB456",
      |      "taxableLumpSumsAndCertainIncome":
      |         {
      |           "amount": 5000.99,
      |           "taxPaid": 3333.33,
      |           "taxTakenOffInEmployment": true
      |         },
      |      "benefitFromEmployerFinancedRetirementScheme":
      |         {
      |           "amount": 5000.99,
      |           "exemptAmount": 2345.99,
      |           "taxPaid": 3333.33,
      |           "taxTakenOffInEmployment": true
      |         },
      |      "redundancyCompensationPaymentsOverExemption":
      |         {
      |           "amount": 5000.99,
      |           "taxPaid": 3333.33,
      |           "taxTakenOffInEmployment": true
      |         },
      |      "redundancyCompensationPaymentsUnderExemption":
      |         {
      |           "amount": 5000.99
      |         }
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val shareOptionModel = Seq(
    AmendShareOptionItem(
      employerName = "Company Ltd",
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
  )

  private val sharesAwardedOrReceivedModel = Seq(
    AmendSharesAwardedOrReceivedItem(
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
  )

  private val disability = AmendCommonOtherEmployment(customerReference = Some("cust ref"), amountDeducted = 1223.22)

  private val foreignService = AmendCommonOtherEmployment(customerReference = Some("cust ref"), amountDeducted = 1223.22)

  private val taxableLumpSumsAndCertainIncome = AmendTaxableLumpSumsAndCertainIncomeItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  private val benefitFromEmployerFinancedRetirementScheme = AmendBenefitFromEmployerFinancedRetirementSchemeItem(
    amount = 5000.99,
    exemptAmount = Some(2345.99),
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  private val redundancyCompensationPaymentsOverExemption = AmendRedundancyCompensationPaymentsOverExemptionItem(
    amount = 5000.99,
    taxPaid = Some(3333.33),
    taxTakenOffInEmployment = true
  )

  private val redundancyCompensationPaymentsUnderExemption = AmendRedundancyCompensationPaymentsUnderExemptionItem(
    amount = 5000.99
  )

  private val lumpSums = Seq(
    AmendLumpSums(
      employerName = "BPDTS Ltd",
      employerRef = "123/AB456",
      taxableLumpSumsAndCertainIncome = Some(taxableLumpSumsAndCertainIncome),
      benefitFromEmployerFinancedRetirementScheme = Some(benefitFromEmployerFinancedRetirementScheme),
      redundancyCompensationPaymentsOverExemption = Some(redundancyCompensationPaymentsOverExemption),
      redundancyCompensationPaymentsUnderExemption = Some(redundancyCompensationPaymentsUnderExemption)
    )
  )

  private val requestBodyModel = AmendOtherEmploymentRequestBody(
    Some(shareOptionModel),
    Some(sharesAwardedOrReceivedModel),
    Some(disability),
    Some(foreignService),
    Some(lumpSums)
  )

  "AmendOtherEmploymentRequestBody" when {
    "read from valid JSON" should {
      "produce the expected AmendOtherEmploymentRequestBody object" in {
        json.as[AmendOtherEmploymentRequestBody] shouldBe requestBodyModel
      }
    }

    "read from empty JSON" should {
      "produce an empty AmendOtherEmploymentRequestBody object" in {
        val emptyJson = JsObject.empty

        emptyJson.as[AmendOtherEmploymentRequestBody] shouldBe AmendOtherEmploymentRequestBody.empty
      }
    }

    "read from valid JSON with empty shareOption and sharesAwardedOrReceived arrays" should {
      "produce an empty AmendOtherEmploymentRequestBody object" in {
        val json = Json.parse(
          """
          |{
          |   "shareOption": [ ],
          |   "sharesAwardedOrReceived": [ ]
          |}
        """.stripMargin
        )

        json.as[AmendOtherEmploymentRequestBody] shouldBe AmendOtherEmploymentRequestBody.empty
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val json = Json.parse(
          """
            |{
            |   "shareOption": [
            |      {
            |        "employerName": false,
            |        "employerRef" : "AB1321/123",
            |        "schemePlanType": "EMI",
            |        "dateOfOptionGrant": "2019-11-20",
            |        "dateOfEvent": "2019-11-20",
            |        "optionNotExercisedButConsiderationReceived": true,
            |        "amountOfConsiderationReceived": 23122.22,
            |        "noOfSharesAcquired": 1,
            |        "classOfSharesAcquired": "FIRST",
            |        "exercisePrice": 12.22,
            |        "amountPaidForOption": 123.22,
            |        "marketValueOfSharesOnExcise": 1232.22,
            |        "profitOnOptionExercised": 1232.33,
            |        "employersNicPaid": 2312.22,
            |        "taxableAmount" : 2132.22
            |      }
            |   ]
            |}
      """.stripMargin
        )

        json.validate[AmendOtherEmploymentRequestBody] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(requestBodyModel) shouldBe json
      }
    }
  }

}
