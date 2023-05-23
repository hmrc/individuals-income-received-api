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

package v1andv2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.MockAuditService
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v1andv2.mocks.requestParsers.MockAmendFinancialDetailsRequestParser
import v1andv2.mocks.services.MockAmendFinancialDetailsService
import v1andv2.models.request.amendFinancialDetails.emploment.studentLoans.AmendStudentLoans
import v1andv2.models.request.amendFinancialDetails.emploment.{AmendBenefitsInKind, AmendDeductions, AmendEmployment, AmendPay}
import v1andv2.models.request.amendFinancialDetails.{AmendFinancialDetailsRawData, AmendFinancialDetailsRequest, AmendFinancialDetailsRequestBody}
import v1andv2.models.response.amendFinancialDetails.AmendFinancialDetailsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendFinancialDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockAmendFinancialDetailsRequestParser
    with MockAmendFinancialDetailsService
    with MockHateoasFactory
    with MockAuditService {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val requestBodyJsonWithOpw: JsValue = Json.parse(
    """
      |{
      |    "employment": {
      |        "pay": {
      |            "taxablePayToDate": 3500.75,
      |            "totalTaxToDate": 6782.92
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
      |        },
      |        "offPayrollWorker": true
      |    }
      |}
    """.stripMargin
  )

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "employment": {
      |        "pay": {
      |            "taxablePayToDate": 3500.75,
      |            "totalTaxToDate": 6782.92
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

  val rawDataOwpEnabled: AmendFinancialDetailsRawData = AmendFinancialDetailsRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId,
    body = AnyContentAsJson(requestBodyJsonWithOpw),
    opwEnabled = true
  )

  val pay: AmendPay = AmendPay(
    taxablePayToDate = 3500.75,
    totalTaxToDate = 6782.92
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
    benefitsInKind = Some(benefitsInKind),
    offPayrollWorker = None
  )

  val amendFinancialDetailsRequestBody: AmendFinancialDetailsRequestBody = AmendFinancialDetailsRequestBody(
    employment = employment
  )

  val requestData: AmendFinancialDetailsRequest = AmendFinancialDetailsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = employmentId,
    body = amendFinancialDetailsRequestBody
  )

  val requestDataWithOpw: AmendFinancialDetailsRequest =
    requestData.copy(body = amendFinancialDetailsRequestBody.copy(employment = employment.copy(offPayrollWorker = Some(true))))

  override val testHateoasLinks: Seq[Link] = Seq(
    hateoas.Link(href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details", method = GET, rel = "self"),
    hateoas.Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
      method = PUT,
      rel = "create-and-amend-employment-financial-details"
    ),
    hateoas.Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
      method = DELETE,
      rel = "delete-employment-financial-details"
    )
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

  "AmendFinancialDetailsController with Opw disabled" should {
    "return OK" when {
      "happy path in non opw test" in new PreOpwTest {
        MockAmendFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendFinancialDetailsService
          .amendFinancialDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendFinancialDetailsHateoasData(nino, taxYear, employmentId))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(hateoasResponse),
          maybeAuditResponseBody = Some(hateoasResponse)
        )
      }

      "happy path in opw test" in new OpwTest {
        MockAmendFinancialDetailsRequestParser
          .parse(rawDataOwpEnabled)
          .returns(Right(requestDataWithOpw))

        MockAmendFinancialDetailsService
          .amendFinancialDetails(requestDataWithOpw)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendFinancialDetailsHateoasData(nino, taxYear, employmentId))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJsonWithOpw),
          maybeExpectedResponseBody = Some(hateoasResponse),
          maybeAuditResponseBody = Some(hateoasResponse)
        )
      }
    }

    "Payload submitted with offPayrollWorker property" when {
      "opw feature switch is disabled" must {
        "return  RuleNotAllowedOffPayrollWorker error" in new PreOpwTest {
          val error: MtdError = RuleNotAllowedOffPayrollWorker
          MockAmendFinancialDetailsRequestParser
            .parse(
              AmendFinancialDetailsRawData(
                nino = nino,
                taxYear = taxYear,
                employmentId = employmentId,
                body = AnyContentAsJson(requestBodyJsonWithOpw),
                opwEnabled = false
              ))
            .returns(Left(ErrorWrapper(correlationId, error, None)))

          override def callController(): Future[Result] =
            controller.amendFinancialDetails(nino, taxYear, employmentId)(fakePutRequest(requestBodyJsonWithOpw))

          runErrorTest(error)
        }
      }
    }

    "Payload submitted without offPayrollWorker property" when {
      "the opw feature switch is enabled and the tax year is 2024" must {
        "return  RuleMissingOffPayrollWorker error" in new OpwTest {
          val error: MtdError = RuleMissingOffPayrollWorker
          MockAmendFinancialDetailsRequestParser
            .parse(
              AmendFinancialDetailsRawData(
                nino = nino,
                taxYear = taxYear,
                employmentId = employmentId,
                body = AnyContentAsJson(requestBodyJson),
                opwEnabled = true
              ))
            .returns(Left(ErrorWrapper(correlationId, error, None)))

          override def callController(): Future[Result] =
            controller.amendFinancialDetails(nino, taxYear, employmentId)(fakePutRequest(requestBodyJson))

          runErrorTest(error)
        }
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new PreOpwTest {
        MockAmendFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new PreOpwTest {
        MockAmendFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendFinancialDetailsService
          .amendFinancialDetails(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))))

        runErrorTestWithAudit(RuleTaxYearNotEndedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new AmendFinancialDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      parser = mockAmendFinancialDetailsRequestParser,
      service = mockAmendFinancialDetailsService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

  }

  trait OpwTest extends Test {

    MockedAppConfig.featureSwitches
      .returns(Configuration("allowTemporalValidationSuspension.enabled" -> true, "tys-api.enabled" -> true))
      .anyNumberOfTimes()

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendEmploymentFinancialDetails",
        transactionName = "amend-employment-financial-details",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

    protected def callController(): Future[Result] =
      controller.amendFinancialDetails(nino, taxYear, employmentId)(fakePutRequest(requestBodyJsonWithOpw))

  }

  trait PreOpwTest extends Test {

    MockedAppConfig.featureSwitches
      .returns(Configuration("opw.enabled" -> false, "allowTemporalValidationSuspension.enabled" -> true, "tys-api.enabled" -> true))
      .anyNumberOfTimes()

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendEmploymentFinancialDetails",
        transactionName = "amend-employment-financial-details",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

    protected def callController(): Future[Result] = controller.amendFinancialDetails(nino, taxYear, employmentId)(fakePutRequest(requestBodyJson))
  }

}
