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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.MockAuditService
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v1.mocks.requestParsers.MockAmendOtherEmploymentRequestParser
import v1.mocks.services.MockAmendOtherEmploymentService
import v1.models.request.amendOtherEmployment._
import v1.models.response.amendOtherEmployment.AmendOtherEmploymentHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendOtherEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockAmendOtherEmploymentRequestParser
    with MockAuditService
    with MockAmendOtherEmploymentService
    with MockHateoasFactory {

  val taxYear: String = "2019-20"

  private val requestBodyJson: JsValue = Json.parse(
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

  val rawData: AmendOtherEmploymentRawData = AmendOtherEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val shareOptionItem: Seq[AmendShareOptionItem] = Seq(
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

  val sharesAwardedOrReceivedItem: Seq[AmendSharesAwardedOrReceivedItem] = Seq(
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

  val disability: AmendCommonOtherEmployment =
    AmendCommonOtherEmployment(
      customerReference = Some("OTHEREmp123A"),
      amountDeducted = 5000.99
    )

  val foreignService: AmendCommonOtherEmployment =
    AmendCommonOtherEmployment(
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

  val amendOtherEmploymentRequestBody: AmendOtherEmploymentRequestBody = AmendOtherEmploymentRequestBody(
    shareOption = Some(shareOptionItem),
    sharesAwardedOrReceived = Some(sharesAwardedOrReceivedItem),
    disability = Some(disability),
    foreignService = Some(foreignService),
    lumpSums = Some(lumpSums)
  )

  val requestData: AmendOtherEmploymentRequest = AmendOtherEmploymentRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = amendOtherEmploymentRequestBody
  )

  override val testHateoasLinks: Seq[Link] = Seq(
    Link(href = s"/individuals/income-received/employments/other/$nino/$taxYear", method = PUT, rel = "create-and-amend-other-employment-income"),
    Link(href = s"/individuals/income-received/employments/other/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/income-received/employments/other/$nino/$taxYear", method = DELETE, rel = "delete-other-employment-income")
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/baseUrl/employments/other/$nino/$taxYear",
       |         "rel":"create-and-amend-employments-other-income",
       |         "method":"PUT"
       |      },
       |      {
       |         "href":"/baseUrl/employments/other/$nino/$taxYear",
       |         "rel":"self",
       |         "method":"GET"
       |      },
       |      {
       |         "href":"/baseUrl/employments/other/$nino/$taxYear",
       |         "rel":"delete-employments-other-income",
       |         "method":"DELETE"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  "AmendOtherEmploymentController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {

        MockAmendOtherEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendOtherEmploymentService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendOtherEmploymentHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(hateoasResponse),
          maybeAuditResponseBody = Some(hateoasResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        MockAmendOtherEmploymentRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        MockAmendOtherEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendOtherEmploymentService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    val controller = new AmendOtherEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendOtherEmploymentRequestParser,
      service = mockAmendOtherEmploymentService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.amendOtherEmployment(nino, taxYear)(fakePutRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendOtherEmployment",
        transactionName = "amend-other-employment",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

}
