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

package v1.controllers.requestParsers

import api.models.domain.{Nino, TaxYear}
import api.models.errors
import api.models.errors._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.mocks.validators.MockAmendOtherEmploymentValidator
import v1.models.request.amendOtherEmployment._

class AmendOtherEmploymentRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
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
      |     {
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

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)

  val rawData: AmendOtherEmploymentRawData = AmendOtherEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  private val shareOptionItem = Seq(
    AmendShareOptionItem(
      employerName = "Company Ltd",
      employerRef = Some("123/AB456"),
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
    ),
    AmendShareOptionItem(
      employerName = "SecondCom Ltd",
      employerRef = Some("123/AB456"),
      schemePlanType = "CSOP",
      dateOfOptionGrant = "2020-09-12",
      dateOfEvent = "2020-09-12",
      optionNotExercisedButConsiderationReceived = false,
      amountOfConsiderationReceived = 5000.99,
      noOfSharesAcquired = 200,
      classOfSharesAcquired = "Ordinary shares",
      exercisePrice = 1000.99,
      amountPaidForOption = 5000.99,
      marketValueOfSharesOnExcise = 5500.99,
      profitOnOptionExercised = 3333.33,
      employersNicPaid = 2000.22,
      taxableAmount = 2555.55
    )
  )

  private val sharesAwardedOrReceivedItem = Seq(
    AmendSharesAwardedOrReceivedItem(
      employerName = "Company Ltd",
      employerRef = Some("123/AB456"),
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
    ),
    AmendSharesAwardedOrReceivedItem(
      employerName = "SecondCom Ltd",
      employerRef = Some("123/AB456"),
      schemePlanType = "Other",
      dateSharesCeasedToBeSubjectToPlan = "2020-09-12",
      noOfShareSecuritiesAwarded = 299,
      classOfShareAwarded = "Ordinary shares",
      dateSharesAwarded = "2020-09-12",
      sharesSubjectToRestrictions = false,
      electionEnteredIgnoreRestrictions = true,
      actualMarketValueOfSharesOnAward = 5000.99,
      unrestrictedMarketValueOfSharesOnAward = 5432.21,
      amountPaidForSharesOnAward = 6000.99,
      marketValueAfterRestrictionsLifted = 3333.33,
      taxableAmount = 98765.99
    )
  )

  private val disability = AmendCommonOtherEmployment(
    customerReference = Some("OTHEREmp123A"),
    amountDeducted = 5000.99
  )

  private val foreignService = AmendCommonOtherEmployment(
    customerReference = Some("OTHEREmp999A"),
    amountDeducted = 7000.99
  )

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

  private val amendOtherEmploymentRequestBody = AmendOtherEmploymentRequestBody(
    shareOption = Some(shareOptionItem),
    sharesAwardedOrReceived = Some(sharesAwardedOrReceivedItem),
    disability = Some(disability),
    foreignService = Some(foreignService),
    lumpSums = Some(lumpSums)
  )

  private val requestData = AmendOtherEmploymentRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = amendOtherEmploymentRequestBody
  )

  trait Test extends MockAmendOtherEmploymentValidator {

    lazy val parser: AmendOtherEmploymentRequestParser = new AmendOtherEmploymentRequestParser(
      validator = mockAmendOtherEmploymentValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendOtherEmploymentValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe Right(requestData)
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendOtherEmploymentValidator
          .validate(rawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(rawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockAmendOtherEmploymentValidator
          .validate(rawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(rawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple field value validation errors occur" in new Test {
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
            |    },
            |   "lumpSums": [
            |    {
            |      "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |      "employerRef" : "InvalidReference",
            |      "taxableLumpSumsAndCertainIncome":
            |         {
            |           "amount": -1111.11,
            |           "taxPaid": 3333.333,
            |           "taxTakenOffInEmployment": true
            |         },
            |      "benefitFromEmployerFinancedRetirementScheme":
            |         {
            |           "amount": -1000.999,
            |           "exemptAmount": 2345.999,
            |           "taxPaid": 123.456,
            |           "taxTakenOffInEmployment": false
            |         },
            |      "redundancyCompensationPaymentsOverExemption":
            |         {
            |           "amount": -2000.22,
            |           "taxPaid": 3333.333,
            |           "taxTakenOffInEmployment": true
            |         },
            |      "redundancyCompensationPaymentsUnderExemption":
            |         {
            |           "amount": -5000.99
            |         }
            |      },
            |      {
            |      "employerName": "This employerName string is 106 characters long--------------------------------------------------------106",
            |      "employerRef" : "InvalidReference",
            |      "taxableLumpSumsAndCertainIncome":
            |         {
            |           "amount": 9999.999,
            |           "taxPaid": 2727.211,
            |           "taxTakenOffInEmployment": false
            |         },
            |      "benefitFromEmployerFinancedRetirementScheme":
            |         {
            |           "amount": -1000.999,
            |           "exemptAmount": -2345.11,
            |           "taxPaid": 5000.555,
            |           "taxTakenOffInEmployment": true
            |         },
            |      "redundancyCompensationPaymentsOverExemption":
            |         {
            |           "amount": -2000.22,
            |           "taxPaid": 3333.333,
            |           "taxTakenOffInEmployment": false
            |         },
            |      "redundancyCompensationPaymentsUnderExemption":
            |         {
            |           "amount": 5000.999
            |         }
            |      }
            |   ]
            |}
            |""".stripMargin
        )

        val allInvalidValueRawRequestBody: AnyContentAsJson = AnyContentAsJson(allInvalidValueRequestBodyJson)

        val allInvalidValueErrors: List[MtdError] = List(
          CustomerRefFormatError.copy(
            paths = Some(
              List(
                "/disability/customerReference",
                "/foreignService/customerReference"
              ))
          ),
          ClassOfSharesAcquiredFormatError.copy(
            paths = Some(
              List(
                "/shareOption/0/classOfSharesAcquired",
                "/shareOption/1/classOfSharesAcquired"
              ))
          ),
          EmployerRefFormatError.copy(
            paths = Some(
              List(
                "/shareOption/0/employerRef",
                "/shareOption/1/employerRef",
                "/sharesAwardedOrReceived/0/employerRef",
                "/sharesAwardedOrReceived/1/employerRef"
              ))
          ),
          SchemePlanTypeFormatError.copy(
            paths = Some(
              List(
                "/shareOption/0/schemePlanType",
                "/shareOption/1/schemePlanType",
                "/sharesAwardedOrReceived/0/schemePlanType",
                "/sharesAwardedOrReceived/1/schemePlanType"
              ))
          ),
          DateFormatError.copy(
            message = "The field should be in the format YYYY-MM-DD",
            paths = Some(
              List(
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
            paths = Some(
              List(
                "/shareOption/0/employerName",
                "/shareOption/1/employerName",
                "/sharesAwardedOrReceived/0/employerName",
                "/sharesAwardedOrReceived/1/employerName"
              ))
          ),
          ClassOfSharesAwardedFormatError.copy(
            paths = Some(
              List(
                "/sharesAwardedOrReceived/0/classOfShareAwarded",
                "/sharesAwardedOrReceived/1/classOfShareAwarded"
              ))
          ),
          ValueFormatError.copy(
            message = "The field should be 0 or more",
            paths = Some(
              List(
                "/shareOption/0/noOfSharesAcquired",
                "/shareOption/1/noOfSharesAcquired",
                "/sharesAwardedOrReceived/0/noOfShareSecuritiesAwarded",
                "/sharesAwardedOrReceived/1/noOfShareSecuritiesAwarded"
              ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(
              List(
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
                "/foreignService/amountDeducted",
                "/lumpSums/0/taxableLumpSumsAndCertainIncome/amount",
                "/lumpSums/0/taxableLumpSumsAndCertainIncome/taxPaid",
                "/lumpSums/0/benefitFromEmployerFinancedRetirementSchemeItem/exemptAmount",
                "/lumpSums/0/benefitFromEmployerFinancedRetirementSchemeItem/taxPaid",
                "/lumpSums/0/redundancyCompensationPaymentsOverExemptionItem/amount",
                "/lumpSums/0/redundancyCompensationPaymentsOverExemptionItem/taxPaid",
                "/lumpSums/0/redundancyCompensationPaymentsUnderExemptionItem/amount",
                "/lumpSums/1/taxableLumpSumsAndCertainIncome/amount",
                "/lumpSums/1/taxableLumpSumsAndCertainIncome/taxPaid",
                "/lumpSums/1/benefitFromEmployerFinancedRetirementSchemeItem/exemptAmount",
                "/lumpSums/1/benefitFromEmployerFinancedRetirementSchemeItem/taxPaid",
                "/lumpSums/1/redundancyCompensationPaymentsOverExemptionItem/amount",
                "/lumpSums/1/redundancyCompensationPaymentsOverExemptionItem/taxPaid",
                "/lumpSums/1/redundancyCompensationPaymentsUnderExemptionItem/amount"
              ))
          )
        )

        MockAmendOtherEmploymentValidator
          .validate(rawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(rawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(errors.ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }

}
