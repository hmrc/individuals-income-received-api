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
import v1.mocks.requestParsers.MockAmendInsurancePoliciesRequestParser
import v1.mocks.services.{MockAmendInsurancePoliciesService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendInsurancePolicies._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendInsurancePoliciesControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAmendInsurancePoliciesService
    with MockAuditService
    with MockAmendInsurancePoliciesRequestParser
    with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "X-123"

  def event(auditRequest: Option[JsValue], auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendInsurancePolicies",
      transactionName = "create-amend-insurance-policies",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        request = auditRequest,
        `X-CorrelationId` = correlationId,
        response = auditResponse
      )
    )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendInsurancePoliciesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendInsurancePoliciesRequestParser,
      service = mockAmendInsurancePoliciesService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockedAppConfig.apiGatewayContext.returns("baseUrl").anyNumberOfTimes()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "lifeAnnuity":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "voidedIsa":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       }
      |   ],
      |   "foreign":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       }
      |   ]
      |}
    """.stripMargin
  )

  val rawData: AmendInsurancePoliciesRawData = AmendInsurancePoliciesRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val lifeInsurance: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val capitalRedemption: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val lifeAnnuity: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )
  val voidedIsa: Seq[AmendVoidedIsaPoliciesItem] = Seq(
    AmendVoidedIsaPoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12)
    ),
    AmendVoidedIsaPoliciesItem(
      customerReference = Some("INPOLY123A"),
      event = Some("Death of spouse"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12)
    )
  )

  val foreign: Seq[AmendForeignPoliciesItem] = Seq(
    AmendForeignPoliciesItem(
      customerReference = Some("INPOLY123A"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15)
    ),
    AmendForeignPoliciesItem(
      customerReference = Some("INPOLY123A"),
      gainAmount = 2000.99,
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15)
    )
  )

  val amendInsurancePoliciesRequestBody: AmendInsurancePoliciesRequestBody = AmendInsurancePoliciesRequestBody(
    lifeInsurance = Some(lifeInsurance),
    capitalRedemption = Some(capitalRedemption),
    lifeAnnuity = Some(lifeAnnuity),
    voidedIsa = Some(voidedIsa),
    foreign = Some(foreign)
  )

  val requestData: AmendInsurancePoliciesRequest = AmendInsurancePoliciesRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = amendInsurancePoliciesRequestBody
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/baseUrl/insurance-policies/$nino/$taxYear",
       |         "rel":"create-and-amend-insurance-policies-income",
       |         "method":"PUT"
       |      },
       |      {
       |         "href":"/baseUrl/insurance-policies/$nino/$taxYear",
       |         "rel":"self",
       |         "method":"GET"
       |      },
       |      {
       |         "href":"/baseUrl/insurance-policies/$nino/$taxYear",
       |         "rel":"delete-insurance-policies-income",
       |         "method":"DELETE"
       |      }
       |   ]
       |}
       |""".stripMargin
  )

  "AmendInsurancePoliciesController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAmendInsurancePoliciesRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendInsurancePoliciesService
          .amendInsurancePolicies(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendInsurancePolicies(nino, taxYear)(fakePutRequest(requestBodyJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)


        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(Some(requestBodyJson), auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendInsurancePoliciesRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.amendInsurancePolicies(nino, taxYear)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(Some(requestBodyJson), auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (CustomerRefFormatError, BAD_REQUEST),
          (EventFormatError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendInsurancePoliciesRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendInsurancePoliciesService
              .amendInsurancePolicies(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.amendInsurancePolicies(nino, taxYear)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)


            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(Some(requestBodyJson), auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}