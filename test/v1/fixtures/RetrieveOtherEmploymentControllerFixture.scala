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

package v1.fixtures

import play.api.libs.json.{JsObject, JsValue, Json}

object RetrieveOtherEmploymentControllerFixture {

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      | "submittedOn": "2020-07-06T09:37:17.000Z",
      | "shareOption":[
      |   {
      |      "employerName": "Company Ltd",
      |      "employerRef" : "123/AB456",
      |      "schemePlanType": "EMI",
      |      "dateOfOptionGrant": "2019-11-20",
      |      "dateOfEvent": "2019-12-22",
      |      "optionNotExercisedButConsiderationReceived": true,
      |      "amountOfConsiderationReceived": 23122.22,
      |      "noOfSharesAcquired": 1,
      |      "classOfSharesAcquired": "FIRST",
      |      "exercisePrice": 12.22,
      |      "amountPaidForOption": 123.22,
      |      "marketValueOfSharesOnExcise": 1232.22,
      |      "profitOnOptionExercised": 1232.33,
      |      "employersNicPaid": 2312.22,
      |      "taxableAmount" : 2132.22
      |   }
      | ],
      | "sharesAwardedOrReceived":[
      |   {
      |     "employerName": "Company Ltd",
      |     "employerRef" : "123/AB456",
      |     "schemePlanType": "SIP",
      |     "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
      |     "noOfShareSecuritiesAwarded": 11,
      |     "classOfShareAwarded": "FIRST",
      |     "dateSharesAwarded" : "2019-12-20",
      |     "sharesSubjectToRestrictions": true,
      |     "electionEnteredIgnoreRestrictions": false,
      |     "actualMarketValueOfSharesOnAward": 2123.22,
      |     "unrestrictedMarketValueOfSharesOnAward": 123.22,
      |     "amountPaidForSharesOnAward": 123.22,
      |     "marketValueAfterRestrictionsLifted": 1232.22,
      |     "taxableAmount": 12321.22
      |   }
      | ],
      | "disability":{
      |   "customerReference": "customer reference",
      |   "amountDeducted": 1223.22
      | },
      | "foreignService":{
      |   "customerReference": "customer reference",
      |   "amountDeducted": 1223.22
      | },
      | "lumpSums": [
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
      |""".stripMargin
  )

  def mtdResponseWithHateoas(nino: String, taxYear: String): JsObject = mtdResponse.as[JsObject] ++ Json
    .parse(
      s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/employments/other/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-employments-other-income"
       |      },
       |      {
       |         "href":"/individuals/income-received/employments/other/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/employments/other/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-employments-other-income"
       |      }
       |   ]
       |}
    """.stripMargin
    )
    .as[JsObject]

}
