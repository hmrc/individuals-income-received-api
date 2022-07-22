/*
 * Copyright 2022 HM Revenue & Customs
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

import api.controllers.ControllerBaseSpec
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditError, AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.HateoasWrapper
import api.models.hateoas.RelType.CREATE_AND_AMEND_UK_SAVINGS
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockCreateAmendUkSavingsAnnualSummaryRequestParser
import v1.mocks.services.MockCreateAmendUkSavingsAnnualSummaryService
import v1.models.request.createAmendUkSavingsAnnualSummary.{
  CreateAmendUkSavingsAnnualSummaryBody,
  CreateAmendUkSavingsAnnualSummaryRawData,
  CreateAmendUkSavingsAnnualSummaryRequest
}
import v1.models.response.createAmendUkSavingsIncomeAnnualSummary.CreateAndAmendUkSavingsAnnualSummaryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendUkSavingsAnnualSummaryControllerSpec
    extends ControllerBaseSpec
    with MockCreateAmendUkSavingsAnnualSummaryService
    with MockCreateAmendUkSavingsAnnualSummaryRequestParser
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAuditService
    with MockHateoasFactory
    with MockIdGenerator {

  val nino: String             = "AA123456A"
  val taxYear: String          = "2019-20"
  val savingsAccountId: String = "acctId"
  val correlationId: String    = "X-123"
  val mtdId: String            = "test-mtd-id"

  val requestJson: JsObject = JsObject.empty

  val rawData: CreateAmendUkSavingsAnnualSummaryRawData = CreateAmendUkSavingsAnnualSummaryRawData(
    nino = nino,
    taxYear = taxYear,
    savingsAccountId = savingsAccountId,
    body = AnyContentAsJson.apply(requestJson)
  )

  val requestData: CreateAmendUkSavingsAnnualSummaryRequest = CreateAmendUkSavingsAnnualSummaryRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    savingsAccountId = savingsAccountId,
    body = CreateAmendUkSavingsAnnualSummaryBody(None, None)
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAmendUkSavingsAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockCreateAmendUkSavingsAnnualSummaryRequestParser,
      service = mockCreateAmendUkSavingsAnnualSummaryService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right(mtdId)))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  def event(auditResponse: AuditResponse): AuditEvent[FlattenedGenericAuditDetail] = {
    AuditEvent(
      auditType = "createAmendUkSavingsAnnualSummary",
      transactionName = CREATE_AND_AMEND_UK_SAVINGS,
      detail = FlattenedGenericAuditDetail(
        versionNumber = Some("1.0"),
        userDetails = UserDetails(mtdId, "Individual", None),
        params = Map("nino" -> nino, "taxYear" -> taxYear, "savingsAccountId" -> savingsAccountId),
        request = None,
        `X-CorrelationId` = correlationId,
        auditResponse = auditResponse
      )
    )
  }

  "CreateAmendUkSavingsAnnualSummaryController" should {
    "return OK" when {
      "happy path" in new Test {

        MockCreateAmendUkSavingsAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendAmendUkSavingsAnnualSummaryService
          .createOrAmendAnnualSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, Unit))))

        MockHateoasFactory
          .wrap((), CreateAndAmendUkSavingsAnnualSummaryHateoasData(nino, taxYear, savingsAccountId))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.createAmendUkSavingsAnnualSummary(nino, taxYear, savingsAccountId)(fakePutRequest(requestJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe testHateoasLinksJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, Right(Some(testHateoasLinksJson)))
        MockedAuditService.verifyAuditEvent[FlattenedGenericAuditDetail](event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateAmendUkSavingsAnnualSummaryRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.createAmendUkSavingsAnnualSummary(nino, taxYear, savingsAccountId)(fakePutRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent[FlattenedGenericAuditDetail](event(auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (SavingsAccountIdFormatError, BAD_REQUEST),
          (withPath(ValueFormatError), BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (withPath(RuleIncorrectOrEmptyBodyError), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateAmendUkSavingsAnnualSummaryRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockCreateAmendAmendUkSavingsAnnualSummaryService
              .createOrAmendAnnualSummary(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.createAmendUkSavingsAnnualSummary(nino, taxYear, savingsAccountId)(fakePutRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
