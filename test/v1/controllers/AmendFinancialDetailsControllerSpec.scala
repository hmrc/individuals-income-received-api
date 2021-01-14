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

package v1.controllers

import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.requestParsers.MockAmendFinancialDetailsRequestParser
import v1.mocks.services.{MockAmendFinancialDetailsService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendFinancialDetails.emploment.studentLoans.AmendStudentLoans
import v1.models.request.amendFinancialDetails.emploment.{AmendBenefitsInKind, AmendDeductions, AmendEmployment, AmendPay}
import v1.models.request.amendFinancialDetails.{AmendFinancialDetailsRawData, AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendFinancialDetailsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAmendFinancialDetailsRequestParser
    with MockAmendFinancialDetailsService
    with MockAuditService
    with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendFinancialDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendFinancialDetailsRequestParser,
      service = mockAmendFinancialDetailsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino = nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "employment": {
      |        "pay": {
      |            "taxablePayToDate": 3500.75,
      |            "totalTaxToDate": 6782.92,
      |            "tipsAndOtherPayments": 1024.99
      |        },
      |        "deductions": {
      |            "studentLoans": {
      |                "uglDeductionAmount": 13343.45,
      |                "pglDeductionAmount": 24242.56
      |            }
      |        },
      |        "benefitsInKind": {
      |            "accommodation": 455.67,
      |            "assets": 435.54,
      |            "assetTransfer": 24.58,
      |            "beneficialLoan": 33.89,
      |            "car": 3434.78,
      |            "carFuel": 34.56,
      |            "educationalServices": 445.67,
      |            "entertaining": 434.45,
      |            "expenses": 3444.32,
      |            "medicalInsurance": 4542.47,
      |            "telephone": 243.43,
      |            "service": 45.67,
      |            "taxableExpenses": 24.56,
      |            "van": 56.29,
      |            "vanFuel": 14.56,
      |            "mileage": 34.23,
      |            "nonQualifyingRelocationExpenses": 54.62,
      |            "nurseryPlaces": 84.29,
      |            "otherItems": 67.67,
      |            "paymentsOnEmployeesBehalf": 67.23,
      |            "personalIncidentalExpenses": 74.29,
      |            "qualifyingRelocationExpenses": 78.24,
      |            "employerProvidedProfessionalSubscriptions": 84.56,
      |            "employerProvidedServices": 56.34,
      |            "incomeTaxPaidByDirector": 67.34,
      |            "travelAndSubsistence": 56.89,
      |            "vouchersAndCreditCards": 34.90,
      |            "nonCash": 23.89
      |        }
      |    }
      |}
    """.stripMargin
  )

  val rawData: AmendFinancialDetailsRawData = AmendFinancialDetailsRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId,
    body = AnyContentAsJson(requestBodyJson)
  )

  val pay: AmendPay = AmendPay(
    taxablePayToDate = 3500.75,
    totalTaxToDate = 6782.92,
    tipsAndOtherPayments = Some(1024.99)
  )

  val studentLoans: AmendStudentLoans = AmendStudentLoans(
    uglDeductionAmount = Some(13343.45),
    pglDeductionAmount = Some(24242.56)
  )

  val deductions: AmendDeductions = AmendDeductions(
    studentLoans = Some(studentLoans)
  )

  val benefitsInKind: AmendBenefitsInKind = AmendBenefitsInKind(
    accommodation = Some(455.67),
    assets = Some(435.54),
    assetTransfer = Some(24.58),
    beneficialLoan = Some(33.89),
    car = Some(3434.78),
    carFuel = Some(34.56),
    educationalServices = Some(445.67),
    entertaining = Some(434.45),
    expenses = Some(3444.32),
    medicalInsurance = Some(4542.47),
    telephone = Some(243.43),
    service = Some(45.67),
    taxableExpenses = Some(24.56),
    van = Some(56.29),
    vanFuel = Some(14.56),
    mileage = Some(34.23),
    nonQualifyingRelocationExpenses = Some(54.62),
    nurseryPlaces = Some(84.29),
    otherItems = Some(67.67),
    paymentsOnEmployeesBehalf = Some(67.23),
    personalIncidentalExpenses = Some(74.29),
    qualifyingRelocationExpenses = Some(78.24),
    employerProvidedProfessionalSubscriptions = Some(84.56),
    employerProvidedServices = Some(56.34),
    incomeTaxPaidByDirector = Some(67.34),
    travelAndSubsistence = Some(56.89),
    vouchersAndCreditCards = Some(34.90),
    nonCash = Some(23.89)
  )

  val employment: AmendEmployment = AmendEmployment(
    pay = pay,
    deductions = Some(deductions),
    benefitsInKind = Some(benefitsInKind)
  )

  val amendFinancialDetailsRequestBody: AmendFinancialDetailsRequestBody = AmendFinancialDetailsRequestBody(
    employment = employment
  )

  val requestData: AmendFinancialDetailsRequest = AmendFinancialDetailsRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId,
    body = amendFinancialDetailsRequestBody
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
       |         "rel":"self",
       |         "method":"GET"
       |      },
       |      {
       |         "href":"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
       |         "rel":"create-and-amend-employment-financial-details",
       |         "method":"PUT"
       |      },
       |      {
       |         "href":"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
       |         "rel":"delete-employment-financial-details",
       |         "method":"DELETE"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "AmendEmploymentFinancialDetails",
      transactionName = "amend-employment-financial-details",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
        request = Some(requestBodyJson),
        `X-CorrelationId` = correlationId,
        response = auditResponse
      )
    )

  "AmendFinancialDetailsController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAmendFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendFinancialDetailsService
          .amendFinancialDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendFinancialDetails(nino, taxYear, employmentId)(fakePutRequest(requestBodyJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendFinancialDetailsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.amendFinancialDetails(nino, taxYear, employmentId)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (EmploymentIdFormatError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotEndedError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendFinancialDetailsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendFinancialDetailsService
              .amendFinancialDetails(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.amendFinancialDetails(nino, taxYear, employmentId)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (RuleTaxYearNotEndedError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}