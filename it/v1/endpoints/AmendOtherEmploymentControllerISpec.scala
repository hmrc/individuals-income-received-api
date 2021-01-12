/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class AmendOtherEmploymentControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2019-20"
    val correlationId: String = "X-123"


    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "shareOption": [
        |      {
        |        "employerName": "Company Ltd",
        |        "employerRef" : "123/AB456",
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
        |      },
        |      {
        |        "employerName": "SecondCom Ltd",
        |        "employerRef" : "123/AB456",
        |        "schemePlanType": "CSOP",
        |        "dateOfOptionGrant": "2020-09-12",
        |        "dateOfEvent": "2020-09-12",
        |        "optionNotExercisedButConsiderationReceived": false,
        |        "amountOfConsiderationReceived": 5000.99,
        |        "noOfSharesAcquired": 200,
        |        "classOfSharesAcquired": "Ordinary shares",
        |        "exercisePrice": 1000.99,
        |        "amountPaidForOption": 5000.99,
        |        "marketValueOfSharesOnExcise": 5500.99,
        |        "profitOnOptionExercised": 3333.33,
        |        "employersNicPaid": 2000.22,
        |        "taxableAmount" : 2555.55
        |      }
        |  ],
        |  "sharesAwardedOrReceived": [
        |       {
        |        "employerName": "Company Ltd",
        |        "employerRef" : "123/AB456",
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
        |       },
        |       {
        |        "employerName": "SecondCom Ltd",
        |        "employerRef" : "123/AB456",
        |        "schemePlanType": "Other",
        |        "dateSharesCeasedToBeSubjectToPlan": "2020-09-12",
        |        "noOfShareSecuritiesAwarded": 299,
        |        "classOfShareAwarded": "Ordinary shares",
        |        "dateSharesAwarded" : "2020-09-12",
        |        "sharesSubjectToRestrictions": false,
        |        "electionEnteredIgnoreRestrictions": true,
        |        "actualMarketValueOfSharesOnAward": 5000.99,
        |        "unrestrictedMarketValueOfSharesOnAward": 5432.21,
        |        "amountPaidForSharesOnAward": 6000.99,
        |        "marketValueAfterRestrictionsLifted": 3333.33,
        |        "taxableAmount": 98765.99
        |       }
        |  ],
        |  "disability":
        |    {
        |      "customerReference": "OTHEREmp123A",
        |      "amountDeducted": 5000.99
        |    },
        |  "foreignService":
        |    {
        |      "customerReference": "OTHEREmp999A",
        |      "amountDeducted": 7000.99
        |    },
        |   "lumpSums": [
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

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |    "links":[
         |        {
         |            "href":"/individuals/income-received/employments/other/AA123456A/2019-20",
         |            "method":"PUT",
         |            "rel":"create-and-amend-employments-other-income"
         |        },
         |        {
         |            "href":"/individuals/income-received/employments/other/AA123456A/2019-20",
         |            "method":"GET",
         |            "rel":"self"
         |        },
         |        {
         |            "href":"/individuals/income-received/employments/other/AA123456A/2019-20",
         |            "method":"DELETE",
         |            "rel":"delete-employments-other-income"
         |        }
         |    ]
         |}
    """.stripMargin
    )

    def uri: String = s"/employments/other/$nino/$taxYear"

    def desUri: String = s"/income-tax/income/other/employments/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'amend other employment income' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.body[JsValue] shouldBe hateoasResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 400 with multiple errors" when {
      "all field value validations fail on the request body" in new Test {

        val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |  "shareOption": [
            |      {
            |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |        "employerRef" : "InvalidReference",
            |        "schemePlanType": "NotAScheme",
            |        "dateOfOptionGrant": "19-11-20",
            |        "dateOfEvent": "19-11-20",
            |        "optionNotExercisedButConsiderationReceived": false,
            |        "amountOfConsiderationReceived": -23122.22,
            |        "noOfSharesAcquired": -100,
            |        "classOfSharesAcquired": "This ClassOfShares Acquired string is 91 characters long ---------------------------------91",
            |        "exercisePrice": 12.222,
            |        "amountPaidForOption": -123.22,
            |        "marketValueOfSharesOnExcise": -1232.222,
            |        "profitOnOptionExercised": -1232.33,
            |        "employersNicPaid": 2312.222,
            |        "taxableAmount" : -2132.222
            |      },
            |      {
            |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |        "employerRef" : "InvalidReference",
            |        "schemePlanType": "NotAScheme",
            |        "dateOfOptionGrant": "20-9-12",
            |        "dateOfEvent": "20-9-12",
            |        "optionNotExercisedButConsiderationReceived": false,
            |        "amountOfConsiderationReceived": 5000.999,
            |        "noOfSharesAcquired": -200,
            |        "classOfSharesAcquired": "",
            |        "exercisePrice": -1000.99,
            |        "amountPaidForOption": 5000.999,
            |        "marketValueOfSharesOnExcise": -5500.999,
            |        "profitOnOptionExercised": -3333.333,
            |        "employersNicPaid": 2000.222,
            |        "taxableAmount" : -2555.55
            |      }
            |  ],
            |  "sharesAwardedOrReceived": [
            |       {
            |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |        "employerRef" : "InvalidReference",
            |        "schemePlanType": "NotAScheme",
            |        "dateSharesCeasedToBeSubjectToPlan": "19-11-10",
            |        "noOfShareSecuritiesAwarded": -11,
            |        "classOfShareAwarded": "This ClassOfShares Acquired string is 91 characters long ---------------------------------91",
            |        "dateSharesAwarded" : "19-11-20",
            |        "sharesSubjectToRestrictions": false,
            |        "electionEnteredIgnoreRestrictions": false,
            |        "actualMarketValueOfSharesOnAward": -2123.22,
            |        "unrestrictedMarketValueOfSharesOnAward": 123.222,
            |        "amountPaidForSharesOnAward": -123.222,
            |        "marketValueAfterRestrictionsLifted": -1232.222,
            |        "taxableAmount": -12321.22
            |       },
            |       {
            |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |        "employerRef" : "InvalidReference",
            |        "schemePlanType": "NotAScheme",
            |        "dateSharesCeasedToBeSubjectToPlan": "20-9-12",
            |        "noOfShareSecuritiesAwarded": -299,
            |        "classOfShareAwarded": "",
            |        "dateSharesAwarded" : "20-09-12",
            |        "sharesSubjectToRestrictions": false,
            |        "electionEnteredIgnoreRestrictions": false,
            |        "actualMarketValueOfSharesOnAward": -5000.999,
            |        "unrestrictedMarketValueOfSharesOnAward": -5432.21,
            |        "amountPaidForSharesOnAward": -6000.99,
            |        "marketValueAfterRestrictionsLifted": -3333.333,
            |        "taxableAmount": 98765.999
            |       }
            |  ],
            |  "disability":
            |    {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "amountDeducted": -5000.99
            |    },
            |  "foreignService":
            |    {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "amountDeducted": 7000.999
            |    }
            |}
            |""".stripMargin
        )

        val allInvalidValueRequestError: List[MtdError] = List(
          EmployerRefFormatError.copy(
            paths = Some(List(
              "/shareOption/0/employerRef",
              "/shareOption/1/employerRef",
              "/sharesAwardedOrReceived/0/employerRef",
              "/sharesAwardedOrReceived/1/employerRef"
            ))
          ),
          CustomerRefFormatError.copy(
            paths = Some(List(
              "/disability/customerReference",
              "/foreignService/customerReference"
            ))
          ),
          ClassOfSharesAcquiredFormatError.copy(
            paths = Some(List(
              "/shareOption/0/classOfSharesAcquired",
              "/shareOption/1/classOfSharesAcquired"
            ))
          ),
          SchemePlanTypeFormatError.copy(
            paths = Some(List(
              "/shareOption/0/schemePlanType",
              "/shareOption/1/schemePlanType",
              "/sharesAwardedOrReceived/0/schemePlanType",
              "/sharesAwardedOrReceived/1/schemePlanType"
            ))
          ),
          DateFormatError.copy(
            message = "The field should be in the format YYYY-MM-DD",
            paths = Some(List(
              "/shareOption/0/dateOfOptionGrant",
              "/shareOption/0/dateOfEvent",
              "/shareOption/1/dateOfOptionGrant",
              "/shareOption/1/dateOfEvent",
              "/sharesAwardedOrReceived/0/dateSharesCeasedToBeSubjectToPlan",
              "/sharesAwardedOrReceived/0/dateSharesAwarded",
              "/sharesAwardedOrReceived/1/dateSharesCeasedToBeSubjectToPlan",
              "/sharesAwardedOrReceived/1/dateSharesAwarded"
            ))
          ),
          EmployerNameFormatError.copy(
            paths = Some(List(
              "/shareOption/0/employerName",
              "/shareOption/1/employerName",
              "/sharesAwardedOrReceived/0/employerName",
              "/sharesAwardedOrReceived/1/employerName"
            ))
          ),
          ClassOfSharesAwardedFormatError.copy(
            paths = Some(List(
              "/sharesAwardedOrReceived/0/classOfShareAwarded",
              "/sharesAwardedOrReceived/1/classOfShareAwarded"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be 0 or more",
            paths = Some(List(
              "/shareOption/0/noOfSharesAcquired",
              "/shareOption/1/noOfSharesAcquired",
              "/sharesAwardedOrReceived/0/noOfShareSecuritiesAwarded",
              "/sharesAwardedOrReceived/1/noOfShareSecuritiesAwarded"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(List(
              "/shareOption/0/amountOfConsiderationReceived",
              "/shareOption/0/exercisePrice",
              "/shareOption/0/amountPaidForOption",
              "/shareOption/0/marketValueOfSharesOnExcise",
              "/shareOption/0/profitOnOptionExercised",
              "/shareOption/0/employersNicPaid",
              "/shareOption/0/taxableAmount",
              "/shareOption/1/amountOfConsiderationReceived",
              "/shareOption/1/exercisePrice",
              "/shareOption/1/amountPaidForOption",
              "/shareOption/1/marketValueOfSharesOnExcise",
              "/shareOption/1/profitOnOptionExercised",
              "/shareOption/1/employersNicPaid",
              "/shareOption/1/taxableAmount",

              "/sharesAwardedOrReceived/0/actualMarketValueOfSharesOnAward",
              "/sharesAwardedOrReceived/0/unrestrictedMarketValueOfSharesOnAward",
              "/sharesAwardedOrReceived/0/amountPaidForSharesOnAward",
              "/sharesAwardedOrReceived/0/marketValueAfterRestrictionsLifted",
              "/sharesAwardedOrReceived/0/taxableAmount",
              "/sharesAwardedOrReceived/1/actualMarketValueOfSharesOnAward",
              "/sharesAwardedOrReceived/1/unrestrictedMarketValueOfSharesOnAward",
              "/sharesAwardedOrReceived/1/amountPaidForSharesOnAward",
              "/sharesAwardedOrReceived/1/marketValueAfterRestrictionsLifted",
              "/sharesAwardedOrReceived/1/taxableAmount",

              "/disability/amountDeducted",
              "/foreignService/amountDeducted"
            ))
          )
        )

        val wrappedErrors: ErrorWrapper = ErrorWrapper(
          correlationId = correlationId,
          error = BadRequestError,
          errors = Some(allInvalidValueRequestError)
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(allInvalidValueRequestBodyJson))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(wrappedErrors)
      }

      "complex error scenario" in new Test {

        val iirOtherEmploymentIncomeAmendErrorsRequest: JsValue = Json.parse(
          """
            |{
            |  "shareOption": [
            |      {
            |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |        "employerRef" : "InvalidReference",
            |        "schemePlanType": "NotAScheme",
            |        "dateOfOptionGrant": "19-11-20",
            |        "dateOfEvent": "19-11-20",
            |        "optionNotExercisedButConsiderationReceived": false,
            |        "amountOfConsiderationReceived": -23122.22,
            |        "noOfSharesAcquired": -100,
            |        "classOfSharesAcquired": "This ClassOfShares Acquired string is 91 characters long ---------------------------------91",
            |        "exercisePrice": 12.222,
            |        "amountPaidForOption": -123.22,
            |        "marketValueOfSharesOnExcise": -1232.222,
            |        "profitOnOptionExercised": -1232.33,
            |        "employersNicPaid": 2312.222,
            |        "taxableAmount" : -2132.222
            |      },
            |      {
            |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |        "employerRef" : "InvalidReference",
            |        "schemePlanType": "NotAScheme",
            |        "dateOfOptionGrant": "20-9-12",
            |        "dateOfEvent": "20-9-12",
            |        "optionNotExercisedButConsiderationReceived": false,
            |        "amountOfConsiderationReceived": 5000.999,
            |        "noOfSharesAcquired": -200,
            |        "classOfSharesAcquired": "",
            |        "exercisePrice": -1000.99,
            |        "amountPaidForOption": 5000.999,
            |        "marketValueOfSharesOnExcise": -5500.999,
            |        "profitOnOptionExercised": -3333.333,
            |        "employersNicPaid": 2000.222,
            |        "taxableAmount" : -2555.55
            |      }
            |  ],
            |  "sharesAwardedOrReceived": [
            |       {
            |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |        "employerRef" : "InvalidReference",
            |        "schemePlanType": "NotAScheme",
            |        "dateSharesCeasedToBeSubjectToPlan": "19-11-10",
            |        "noOfShareSecuritiesAwarded": -11,
            |        "classOfShareAwarded": "This ClassOfShares Acquired string is 91 characters long ---------------------------------91",
            |        "dateSharesAwarded" : "19-11-20",
            |        "sharesSubjectToRestrictions": false,
            |        "electionEnteredIgnoreRestrictions": false,
            |        "actualMarketValueOfSharesOnAward": -2123.22,
            |        "unrestrictedMarketValueOfSharesOnAward": 123.222,
            |        "amountPaidForSharesOnAward": -123.222,
            |        "marketValueAfterRestrictionsLifted": -1232.222,
            |        "taxableAmount": -12321.22
            |       },
            |       {
            |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |        "employerRef" : "InvalidReference",
            |        "schemePlanType": "NotAScheme",
            |        "dateSharesCeasedToBeSubjectToPlan": "20-9-12",
            |        "noOfShareSecuritiesAwarded": -299,
            |        "classOfShareAwarded": "",
            |        "dateSharesAwarded" : "20-09-12",
            |        "sharesSubjectToRestrictions": false,
            |        "electionEnteredIgnoreRestrictions": false,
            |        "actualMarketValueOfSharesOnAward": -5000.999,
            |        "unrestrictedMarketValueOfSharesOnAward": -5432.21,
            |        "amountPaidForSharesOnAward": -6000.99,
            |        "marketValueAfterRestrictionsLifted": -3333.333,
            |        "taxableAmount": 98765.999
            |       }
            |  ],
            |  "disability":
            |    {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "amountDeducted": -5000.99
            |    },
            |  "foreignService":
            |    {
            |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |      "amountDeducted": 7000.999
            |    }
            |}
            |""".stripMargin
        )

        val iirOtherEmploymentIncomeAmendErrorsResponse: JsValue = Json.parse(
          """{
            |    "code":"INVALID_REQUEST",
            |    "message":"Invalid request",
            |    "errors":[
            |        {
            |            "code":"FORMAT_EMPLOYER_REF",
            |            "message":"The provided employer ref is invalid",
            |            "paths":[
            |                "/shareOption/0/employerRef",
            |                "/shareOption/1/employerRef",
            |                "/sharesAwardedOrReceived/0/employerRef",
            |                "/sharesAwardedOrReceived/1/employerRef"
            |            ]
            |        },
            |        {
            |            "code":"FORMAT_CUSTOMER_REF",
            |            "message":"The provided customer reference is invalid",
            |            "paths":[
            |                "/disability/customerReference",
            |                "/foreignService/customerReference"
            |            ]
            |        },
            |        {
            |            "code":"FORMAT_CLASS_OF_SHARES_ACQUIRED",
            |            "message":"The provided class of shares acquired is invalid",
            |            "paths":[
            |                "/shareOption/0/classOfSharesAcquired",
            |                "/shareOption/1/classOfSharesAcquired"
            |            ]
            |        },
            |        {
            |            "code":"FORMAT_SCHEME_PLAN_TYPE",
            |            "message":"The provided scheme plan type is invalid",
            |            "paths":[
            |                "/shareOption/0/schemePlanType",
            |                "/shareOption/1/schemePlanType",
            |                "/sharesAwardedOrReceived/0/schemePlanType",
            |                "/sharesAwardedOrReceived/1/schemePlanType"
            |            ]
            |        },
            |        {
            |            "code":"FORMAT_DATE",
            |            "message":"The field should be in the format YYYY-MM-DD",
            |            "paths":[
            |                "/shareOption/0/dateOfOptionGrant",
            |                "/shareOption/0/dateOfEvent",
            |                "/shareOption/1/dateOfOptionGrant",
            |                "/shareOption/1/dateOfEvent",
            |                "/sharesAwardedOrReceived/0/dateSharesCeasedToBeSubjectToPlan",
            |                "/sharesAwardedOrReceived/0/dateSharesAwarded",
            |                "/sharesAwardedOrReceived/1/dateSharesCeasedToBeSubjectToPlan",
            |                "/sharesAwardedOrReceived/1/dateSharesAwarded"
            |            ]
            |        },
            |        {
            |            "code":"FORMAT_EMPLOYER_NAME",
            |            "message":"The provided employer name is invalid",
            |            "paths":[
            |                "/shareOption/0/employerName",
            |                "/shareOption/1/employerName",
            |                "/sharesAwardedOrReceived/0/employerName",
            |                "/sharesAwardedOrReceived/1/employerName"
            |            ]
            |        },
            |        {
            |            "code":"FORMAT_CLASS_OF_SHARES_AWARDED",
            |            "message":"The provided class of shares awarded is invalid",
            |            "paths":[
            |                "/sharesAwardedOrReceived/0/classOfShareAwarded",
            |                "/sharesAwardedOrReceived/1/classOfShareAwarded"
            |            ]
            |        },
            |        {
            |            "code":"FORMAT_VALUE",
            |            "message":"The field should be 0 or more",
            |            "paths":[
            |                "/shareOption/0/noOfSharesAcquired",
            |                "/shareOption/1/noOfSharesAcquired",
            |                "/sharesAwardedOrReceived/0/noOfShareSecuritiesAwarded",
            |                "/sharesAwardedOrReceived/1/noOfShareSecuritiesAwarded"
            |            ]
            |        },
            |        {
            |            "code":"FORMAT_VALUE",
            |            "message":"The field should be between 0 and 99999999999.99",
            |            "paths":[
            |                "/shareOption/0/amountOfConsiderationReceived",
            |                "/shareOption/0/exercisePrice",
            |                "/shareOption/0/amountPaidForOption",
            |                "/shareOption/0/marketValueOfSharesOnExcise",
            |                "/shareOption/0/profitOnOptionExercised",
            |                "/shareOption/0/employersNicPaid",
            |                "/shareOption/0/taxableAmount",
            |                "/shareOption/1/amountOfConsiderationReceived",
            |                "/shareOption/1/exercisePrice",
            |                "/shareOption/1/amountPaidForOption",
            |                "/shareOption/1/marketValueOfSharesOnExcise",
            |                "/shareOption/1/profitOnOptionExercised",
            |                "/shareOption/1/employersNicPaid",
            |                "/shareOption/1/taxableAmount",
            |                "/sharesAwardedOrReceived/0/actualMarketValueOfSharesOnAward",
            |                "/sharesAwardedOrReceived/0/unrestrictedMarketValueOfSharesOnAward",
            |                "/sharesAwardedOrReceived/0/amountPaidForSharesOnAward",
            |                "/sharesAwardedOrReceived/0/marketValueAfterRestrictionsLifted",
            |                "/sharesAwardedOrReceived/0/taxableAmount",
            |                "/sharesAwardedOrReceived/1/actualMarketValueOfSharesOnAward",
            |                "/sharesAwardedOrReceived/1/unrestrictedMarketValueOfSharesOnAward",
            |                "/sharesAwardedOrReceived/1/amountPaidForSharesOnAward",
            |                "/sharesAwardedOrReceived/1/marketValueAfterRestrictionsLifted",
            |                "/sharesAwardedOrReceived/1/taxableAmount",
            |                "/disability/amountDeducted",
            |                "/foreignService/amountDeducted"
            |            ]
            |        }
            |    ]
            |}""".stripMargin
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().put(iirOtherEmploymentIncomeAmendErrorsRequest))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe iirOtherEmploymentIncomeAmendErrorsResponse
      }
    }

    "return error according to spec" when {

      val validRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "shareOption": [
          |      {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "123/AB456",
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
          |      },
          |      {
          |        "employerName": "SecondCom Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "CSOP",
          |        "dateOfOptionGrant": "2020-09-12",
          |        "dateOfEvent": "2020-09-12",
          |        "optionNotExercisedButConsiderationReceived": false,
          |        "amountOfConsiderationReceived": 5000.99,
          |        "noOfSharesAcquired": 200,
          |        "classOfSharesAcquired": "Ordinary shares",
          |        "exercisePrice": 1000.99,
          |        "amountPaidForOption": 5000.99,
          |        "marketValueOfSharesOnExcise": 5500.99,
          |        "profitOnOptionExercised": 3333.33,
          |        "employersNicPaid": 2000.22,
          |        "taxableAmount" : 2555.55
          |      }
          |  ],
          |  "sharesAwardedOrReceived": [
          |       {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "123/AB456",
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
          |       },
          |       {
          |        "employerName": "SecondCom Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "Other",
          |        "dateSharesCeasedToBeSubjectToPlan": "2020-09-12",
          |        "noOfShareSecuritiesAwarded": 299,
          |        "classOfShareAwarded": "Ordinary shares",
          |        "dateSharesAwarded" : "2020-09-12",
          |        "sharesSubjectToRestrictions": false,
          |        "electionEnteredIgnoreRestrictions": true,
          |        "actualMarketValueOfSharesOnAward": 5000.99,
          |        "unrestrictedMarketValueOfSharesOnAward": 5432.21,
          |        "amountPaidForSharesOnAward": 6000.99,
          |        "marketValueAfterRestrictionsLifted": 3333.33,
          |        "taxableAmount": 98765.99
          |       }
          |  ],
          |  "disability":
          |    {
          |      "customerReference": "OTHEREmp123A",
          |      "amountDeducted": 5000.99
          |    },
          |  "foreignService":
          |    {
          |      "customerReference": "OTHEREmp999A",
          |      "amountDeducted": 7000.99
          |    }
          |}
    """.stripMargin
      )

      val invalidEmployerNameRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "shareOption": [
          |      {
          |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
          |        "employerRef" : "123/AB456",
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
          |      },
          |      {
          |        "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
          |        "employerRef" : "987/XZ321",
          |        "schemePlanType": "CSOP",
          |        "dateOfOptionGrant": "2020-09-12",
          |        "dateOfEvent": "2020-09-12",
          |        "optionNotExercisedButConsiderationReceived": false,
          |        "amountOfConsiderationReceived": 5000.99,
          |        "noOfSharesAcquired": 200,
          |        "classOfSharesAcquired": "Ordinary shares",
          |        "exercisePrice": 1000.99,
          |        "amountPaidForOption": 5000.99,
          |        "marketValueOfSharesOnExcise": 5500.99,
          |        "profitOnOptionExercised": 3333.33,
          |        "employersNicPaid": 2000.22,
          |        "taxableAmount" : 2555.55
          |      }
          |   ]
          |}
          |""".stripMargin
      )

      val invalidEmployerRefRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "shareOption": [
          |      {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "InvalidReference",
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
          |      },
          |      {
          |        "employerName": "SecondCom Ltd",
          |        "employerRef" : "ABC393/123",
          |        "schemePlanType": "CSOP",
          |        "dateOfOptionGrant": "2020-09-12",
          |        "dateOfEvent": "2020-09-12",
          |        "optionNotExercisedButConsiderationReceived": false,
          |        "amountOfConsiderationReceived": 5000.99,
          |        "noOfSharesAcquired": 200,
          |        "classOfSharesAcquired": "Ordinary shares",
          |        "exercisePrice": 1000.99,
          |        "amountPaidForOption": 5000.99,
          |        "marketValueOfSharesOnExcise": 5500.99,
          |        "profitOnOptionExercised": 3333.33,
          |        "employersNicPaid": 2000.22,
          |        "taxableAmount" : 2555.55
          |      }
          |   ]
          |}
          |""".stripMargin
      )

      val invalidSchemePlanTypeRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "shareOption": [
          |      {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "NotAScheme",
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
          |      },
          |      {
          |        "employerName": "SecondCom Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "12345",
          |        "dateOfOptionGrant": "2020-09-12",
          |        "dateOfEvent": "2020-09-12",
          |        "optionNotExercisedButConsiderationReceived": false,
          |        "amountOfConsiderationReceived": 5000.99,
          |        "noOfSharesAcquired": 200,
          |        "classOfSharesAcquired": "Ordinary shares",
          |        "exercisePrice": 1000.99,
          |        "amountPaidForOption": 5000.99,
          |        "marketValueOfSharesOnExcise": 5500.99,
          |        "profitOnOptionExercised": 3333.33,
          |        "employersNicPaid": 2000.22,
          |        "taxableAmount" : 2555.55
          |      }
          |   ]
          |}
          |""".stripMargin
      )

      val invalidDateRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "shareOption": [
          |      {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "EMI",
          |        "dateOfOptionGrant": "19-11-20",
          |        "dateOfEvent": "19-11-20",
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
          |      },
          |      {
          |        "employerName": "SecondCom Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "CSOP",
          |        "dateOfOptionGrant": "24-07-2020",
          |        "dateOfEvent": "24-07-2020",
          |        "optionNotExercisedButConsiderationReceived": false,
          |        "amountOfConsiderationReceived": 5000.99,
          |        "noOfSharesAcquired": 200,
          |        "classOfSharesAcquired": "Ordinary shares",
          |        "exercisePrice": 1000.99,
          |        "amountPaidForOption": 5000.99,
          |        "marketValueOfSharesOnExcise": 5500.99,
          |        "profitOnOptionExercised": 3333.33,
          |        "employersNicPaid": 2000.22,
          |        "taxableAmount" : 2555.55
          |      }
          |   ]
          |}
          |""".stripMargin
      )

      val invalidClassOfSharesAcquiredRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "shareOption": [
          |      {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "EMI",
          |        "dateOfOptionGrant": "2019-11-20",
          |        "dateOfEvent": "2019-11-20",
          |        "optionNotExercisedButConsiderationReceived": true,
          |        "amountOfConsiderationReceived": 23122.22,
          |        "noOfSharesAcquired": 1,
          |        "classOfSharesAcquired": "This ClassOfShares Acquired string is 91 characters long ---------------------------------91",
          |        "exercisePrice": 12.22,
          |        "amountPaidForOption": 123.22,
          |        "marketValueOfSharesOnExcise": 1232.22,
          |        "profitOnOptionExercised": 1232.33,
          |        "employersNicPaid": 2312.22,
          |        "taxableAmount" : 2132.22
          |      },
          |      {
          |        "employerName": "SecondCom Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "CSOP",
          |        "dateOfOptionGrant": "2020-09-12",
          |        "dateOfEvent": "2020-09-12",
          |        "optionNotExercisedButConsiderationReceived": false,
          |        "amountOfConsiderationReceived": 5000.99,
          |        "noOfSharesAcquired": 200,
          |        "classOfSharesAcquired": "This ClassOfShares Acquired string is 91 characters long ---------------------------------91",
          |        "exercisePrice": 1000.99,
          |        "amountPaidForOption": 5000.99,
          |        "marketValueOfSharesOnExcise": 5500.99,
          |        "profitOnOptionExercised": 3333.33,
          |        "employersNicPaid": 2000.22,
          |        "taxableAmount" : 2555.55
          |      }
          |
          |   ]
          |}
          |""".stripMargin
      )

      val invalidClassOfSharesAwardedRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "sharesAwardedOrReceived": [
          |      {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "SIP",
          |        "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
          |        "noOfShareSecuritiesAwarded": 11,
          |        "classOfShareAwarded": "This ClassOfShares Awarded string is 91 characters long ---------------------------------91",
          |        "dateSharesAwarded" : "2019-11-20",
          |        "sharesSubjectToRestrictions": true,
          |        "electionEnteredIgnoreRestrictions": false,
          |        "actualMarketValueOfSharesOnAward": 2123.22,
          |        "unrestrictedMarketValueOfSharesOnAward": 123.22,
          |        "amountPaidForSharesOnAward": 123.22,
          |        "marketValueAfterRestrictionsLifted": 1232.22,
          |        "taxableAmount": 12321.22
          |      },
          |      {
          |        "employerName": "SecondCom Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "Other",
          |        "dateSharesCeasedToBeSubjectToPlan": "2020-09-12",
          |        "noOfShareSecuritiesAwarded": 299,
          |        "classOfShareAwarded": "This ClassOfShares Awarded string is 91 characters long ---------------------------------91",
          |        "dateSharesAwarded" : "2020-09-12",
          |        "sharesSubjectToRestrictions": false,
          |        "electionEnteredIgnoreRestrictions": true,
          |        "actualMarketValueOfSharesOnAward": 5000.99,
          |        "unrestrictedMarketValueOfSharesOnAward": 5432.21,
          |        "amountPaidForSharesOnAward": 6000.99,
          |        "marketValueAfterRestrictionsLifted": 3333.33,
          |        "taxableAmount": 98765.99
          |       }
          |   ]
          |}
          |""".stripMargin
      )

      val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "disability":
          |    {
          |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |      "amountDeducted": 5000.99
          |    },
          |  "foreignService":
          |    {
          |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
          |      "amountDeducted": 7000.99
          |    }
          |}
          |""".stripMargin
      )

      val nonsenseRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "field": "value"
          |}
        """.stripMargin
      )

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "shareOption": [
          |      {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "EMI",
          |        "dateOfOptionGrant": "2019-11-20",
          |        "dateOfEvent": "2019-11-20",
          |        "optionNotExercisedButConsiderationReceived": true,
          |        "amountOfConsiderationReceived": 23122.22,
          |        "noOfSharesAcquired": 1,
          |        "classOfSharesAcquired": "FIRST",
          |        "exercisePrice": 12.22,
          |        "amountPaidForOption": 123.22,
          |        "marketValueOfSharesOnExcise": "bip",
          |        "profitOnOptionExercised": "bop",
          |        "employersNicPaid": "boop",
          |        "taxableAmount" : "beep"
          |      }
          |    ]
          |}
        """.stripMargin
      )

      val missingFieldRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "shareOption": [{}]
          |}
        """.stripMargin
      )

      val invalidValuesRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "shareOption": [
          |      {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "EMI",
          |        "dateOfOptionGrant": "2019-11-20",
          |        "dateOfEvent": "2019-11-20",
          |        "optionNotExercisedButConsiderationReceived": true,
          |        "amountOfConsiderationReceived": -23122.22,
          |        "noOfSharesAcquired": -100,
          |        "classOfSharesAcquired": "FIRST",
          |        "exercisePrice": 12.222,
          |        "amountPaidForOption": -123.22,
          |        "marketValueOfSharesOnExcise": -1232.222,
          |        "profitOnOptionExercised": -1232.33,
          |        "employersNicPaid": 2312.222,
          |        "taxableAmount" : -2132.222
          |      },
          |      {
          |        "employerName": "SecondCom Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "CSOP",
          |        "dateOfOptionGrant": "2020-09-12",
          |        "dateOfEvent": "2020-09-12",
          |        "optionNotExercisedButConsiderationReceived": false,
          |        "amountOfConsiderationReceived": 5000.999,
          |        "noOfSharesAcquired": -200,
          |        "classOfSharesAcquired": "Ordinary shares",
          |        "exercisePrice": -1000.99,
          |        "amountPaidForOption": 5000.999,
          |        "marketValueOfSharesOnExcise": -5500.999,
          |        "profitOnOptionExercised": -3333.333,
          |        "employersNicPaid": 2000.222,
          |        "taxableAmount" : -2555.55
          |      }
          |  ],
          |  "sharesAwardedOrReceived": [
          |       {
          |        "employerName": "Company Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "SIP",
          |        "dateSharesCeasedToBeSubjectToPlan": "2019-11-10",
          |        "noOfShareSecuritiesAwarded": -11,
          |        "classOfShareAwarded": "FIRST",
          |        "dateSharesAwarded" : "2019-11-20",
          |        "sharesSubjectToRestrictions": true,
          |        "electionEnteredIgnoreRestrictions": false,
          |        "actualMarketValueOfSharesOnAward": -2123.22,
          |        "unrestrictedMarketValueOfSharesOnAward": 123.222,
          |        "amountPaidForSharesOnAward": -123.222,
          |        "marketValueAfterRestrictionsLifted": -1232.222,
          |        "taxableAmount": -12321.22
          |       },
          |       {
          |        "employerName": "SecondCom Ltd",
          |        "employerRef" : "123/AB456",
          |        "schemePlanType": "Other",
          |        "dateSharesCeasedToBeSubjectToPlan": "2020-09-12",
          |        "noOfShareSecuritiesAwarded": -299,
          |        "classOfShareAwarded": "Ordinary shares",
          |        "dateSharesAwarded" : "2020-09-12",
          |        "sharesSubjectToRestrictions": false,
          |        "electionEnteredIgnoreRestrictions": true,
          |        "actualMarketValueOfSharesOnAward": -5000.999,
          |        "unrestrictedMarketValueOfSharesOnAward": -5432.21,
          |        "amountPaidForSharesOnAward": -6000.99,
          |        "marketValueAfterRestrictionsLifted": -3333.333,
          |        "taxableAmount": 98765.999
          |       }
          |  ],
          |  "disability":
          |    {
          |    "customerReference": "OTHEREmp123A",
          |    "amountDeducted": -5000.99
          |    },
          |  "foreignService":
          |    {
          |      "customerReference": "OTHEREmp999A",
          |      "amountDeducted": 7000.999
          |    }
          |}
          |""".stripMargin
      )

      val customerRefFormatError: MtdError = CustomerRefFormatError.copy(
        paths = Some(Seq(
          "/disability/customerReference",
          "/foreignService/customerReference"
        ))
      )

      val employerRefFormatError: MtdError = EmployerRefFormatError.copy(
        paths = Some(Seq(
          "/shareOption/0/employerRef",
          "/shareOption/1/employerRef"
        ))
      )

      val employerNameFormatError: MtdError =  EmployerNameFormatError.copy(
        paths = Some(Seq(
          "/shareOption/0/employerName",
          "/shareOption/1/employerName"
        ))
      )

      val schemePlanTypeFormatError: MtdError = SchemePlanTypeFormatError.copy(
        paths = Some(Seq(
          "/shareOption/0/schemePlanType",
          "/shareOption/1/schemePlanType"
        ))
      )

      val dateFormatError: MtdError = DateFormatError.copy(
        message = "The field should be in the format YYYY-MM-DD",
        paths = Some(Seq(
          "/shareOption/0/dateOfOptionGrant",
          "/shareOption/0/dateOfEvent",
          "/shareOption/1/dateOfOptionGrant",
          "/shareOption/1/dateOfEvent"
        ))
      )

      val classOfSharesAcquiredFormatError: MtdError = ClassOfSharesAcquiredFormatError.copy(
        paths = Some(Seq(
          "/shareOption/0/classOfSharesAcquired",
          "/shareOption/1/classOfSharesAcquired"
        ))
      )

      val classOfSharesAwardedFormatError: MtdError = ClassOfSharesAwardedFormatError.copy(
        paths = Some(Seq(
          "/sharesAwardedOrReceived/0/classOfShareAwarded",
          "/sharesAwardedOrReceived/1/classOfShareAwarded"
        ))
      )

      val allInvalidValueErrors: Seq[MtdError] = Seq(
        ValueFormatError.copy(
          message = "The field should be 0 or more",
          paths = Some(List(
            "/shareOption/0/noOfSharesAcquired",
            "/shareOption/1/noOfSharesAcquired",
            "/sharesAwardedOrReceived/0/noOfShareSecuritiesAwarded",
            "/sharesAwardedOrReceived/1/noOfShareSecuritiesAwarded"
          ))
        ),
        ValueFormatError.copy(
          message = "The field should be between 0 and 99999999999.99",
          paths = Some(List(
            "/shareOption/0/amountOfConsiderationReceived",
            "/shareOption/0/exercisePrice",
            "/shareOption/0/amountPaidForOption",
            "/shareOption/0/marketValueOfSharesOnExcise",
            "/shareOption/0/profitOnOptionExercised",
            "/shareOption/0/employersNicPaid",
            "/shareOption/0/taxableAmount",
            "/shareOption/1/amountOfConsiderationReceived",
            "/shareOption/1/exercisePrice",
            "/shareOption/1/amountPaidForOption",
            "/shareOption/1/marketValueOfSharesOnExcise",
            "/shareOption/1/profitOnOptionExercised",
            "/shareOption/1/employersNicPaid",
            "/shareOption/1/taxableAmount",

            "/sharesAwardedOrReceived/0/actualMarketValueOfSharesOnAward",
            "/sharesAwardedOrReceived/0/unrestrictedMarketValueOfSharesOnAward",
            "/sharesAwardedOrReceived/0/amountPaidForSharesOnAward",
            "/sharesAwardedOrReceived/0/marketValueAfterRestrictionsLifted",
            "/sharesAwardedOrReceived/0/taxableAmount",
            "/sharesAwardedOrReceived/1/actualMarketValueOfSharesOnAward",
            "/sharesAwardedOrReceived/1/unrestrictedMarketValueOfSharesOnAward",
            "/sharesAwardedOrReceived/1/amountPaidForSharesOnAward",
            "/sharesAwardedOrReceived/1/marketValueAfterRestrictionsLifted",
            "/sharesAwardedOrReceived/1/taxableAmount",

            "/disability/amountDeducted",
            "/foreignService/amountDeducted"
          ))
        )
      )

      val invalidFieldType: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(List(
          "/shareOption/0/profitOnOptionExercised",
          "/shareOption/0/taxableAmount",
          "/shareOption/0/marketValueOfSharesOnExcise",
          "/shareOption/0/employersNicPaid"
        ))
      )

      val missingMandatoryFieldErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(List(
          "/shareOption/0/dateOfOptionGrant",
          "/shareOption/0/taxableAmount",
          "/shareOption/0/employerName",
          "/shareOption/0/noOfSharesAcquired",
          "/shareOption/0/optionNotExercisedButConsiderationReceived",
          "/shareOption/0/schemePlanType",
          "/shareOption/0/classOfSharesAcquired",
          "/shareOption/0/exercisePrice",
          "/shareOption/0/amountPaidForOption",
          "/shareOption/0/employersNicPaid",
          "/shareOption/0/marketValueOfSharesOnExcise",
          "/shareOption/0/amountOfConsiderationReceived",
          "/shareOption/0/dateOfEvent",
          "/shareOption/0/profitOnOptionExercised"
        ))
      )

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, requestBody: JsValue, expectedStatus: Int, expectedBody: ErrorWrapper): Unit = {
          s"validation fails with ${expectedBody.error} error" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear
            override val requestBodyJson: JsValue = requestBody

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          ("AA1123A", "2019-20", validRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", NinoFormatError, None)),
          ("AA123456A", "20177", validRequestBodyJson,  BAD_REQUEST, ErrorWrapper("X-123", TaxYearFormatError, None)),
          ("AA123456A", "2015-17", validRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", RuleTaxYearRangeInvalidError, None)),
          ("AA123456A", "2018-19", validRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", RuleTaxYearNotSupportedError, None)),
          ("AA123456A", "2019-20", invalidValuesRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", BadRequestError, Some(allInvalidValueErrors))),
          ("AA123456A", "2019-20", invalidEmployerNameRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", employerNameFormatError, None)),
          ("AA123456A", "2019-20", invalidEmployerRefRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", employerRefFormatError, None)),
          ("AA123456A", "2019-20", invalidDateRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", dateFormatError, None)),
          ("AA123456A", "2019-20", invalidClassOfSharesAwardedRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", classOfSharesAwardedFormatError, None)),
          ("AA123456A", "2019-20", invalidClassOfSharesAcquiredRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", classOfSharesAcquiredFormatError, None)),
          ("AA123456A", "2019-20", invalidCustomerRefRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", customerRefFormatError, None)),
          ("AA123456A", "2019-20", invalidSchemePlanTypeRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", schemePlanTypeFormatError, None)),
          ("AA123456A", "2019-20", nonsenseRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", RuleIncorrectOrEmptyBodyError, None)),
          ("AA123456A", "2019-20", nonValidRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", invalidFieldType, None)),
          ("AA123456A", "2019-20", missingFieldRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", missingMandatoryFieldErrors, None))
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.PUT, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "des message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
          (UNPROCESSABLE_ENTITY, "UNPROCESSABLE_ENTITY", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}