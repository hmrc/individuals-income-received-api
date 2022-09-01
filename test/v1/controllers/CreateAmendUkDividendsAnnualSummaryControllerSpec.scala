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
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockCreateAmendUkDividendsAnnualSummaryRequestParser
import v1.mocks.services.MockCreateAmendUkDividendsAnnualSummaryService
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary.{
  CreateAmendUkDividendsIncomeAnnualSummaryBody,
  CreateAmendUkDividendsIncomeAnnualSummaryRawData,
  CreateAmendUkDividendsIncomeAnnualSummaryRequest
}
import v1.models.response.createAmendUkDividendsIncomeAnnualSummary.CreateAndAmendUkDividendsIncomeAnnualSummaryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendUkDividendsAnnualSummaryControllerSpec
    extends ControllerBaseSpec
    with MockCreateAmendUkDividendsAnnualSummaryService
    with MockCreateAmendUkDividendsAnnualSummaryRequestParser
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAuditService
    with MockHateoasFactory
    with MockIdGenerator {

  val requestJson: JsObject = JsObject.empty

  trait Test {
    val taxYear: String

    val nino: String          = "AA123456A"
    val correlationId: String = "X-123"
    val mtdId: String         = "test-mtd-id"

    lazy val rawData: CreateAmendUkDividendsIncomeAnnualSummaryRawData = CreateAmendUkDividendsIncomeAnnualSummaryRawData(
      nino = nino,
      taxYear = taxYear,
      body = AnyContentAsJson.apply(requestJson)
    )

    val requestModel: CreateAmendUkDividendsIncomeAnnualSummaryBody = CreateAmendUkDividendsIncomeAnnualSummaryBody(None, None)

    lazy val requestData: CreateAmendUkDividendsIncomeAnnualSummaryRequest = CreateAmendUkDividendsIncomeAnnualSummaryRequest(
      nino = Nino(nino),
      taxYear = TaxYear.fromMtd(taxYear),
      body = requestModel
    )

    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAmendUkDividendsAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockCreateAmendUkDividendsAnnualSummaryRequestParser,
      service = mockCreateAmendUkDividendsAnnualSummaryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator,
      auditService = mockAuditService
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right(mtdId)))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)

    def event(auditResponse: AuditResponse): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAndAmendUkDividendsIncome",
        transactionName = "create-amend-uk-dividends-income",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some("1.0"),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = Some(requestJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

  "CreateAmendUkDividendsAnnualSummaryController" should {
    "return OK" when {

      "happy path for pre-TYS downstream API" in new Test {
        val taxYear: String = "2019-20"

        MockCreateAmendUkDividendsAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendAmendUkDividendsAnnualSummaryService
          .createOrAmendAnnualSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, Unit))))

        MockHateoasFactory
          .wrap((), CreateAndAmendUkDividendsIncomeAnnualSummaryHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.createAmendUkDividendsAnnualSummary(nino, taxYear)(fakePutRequest(requestJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe testHateoasLinksJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, Right(Some(testHateoasLinksJson)))
        MockedAuditService.verifyAuditEvent[FlattenedGenericAuditDetail](event(auditResponse)).once

      }

      "happy path for TYS downstream API" in new Test {
        val taxYear: String = "2023-24"

        MockCreateAmendUkDividendsAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendAmendUkDividendsAnnualSummaryService
          .createOrAmendAnnualSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, Unit))))

        MockHateoasFactory
          .wrap((), CreateAndAmendUkDividendsIncomeAnnualSummaryHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.createAmendUkDividendsAnnualSummary(nino, taxYear)(fakePutRequest(requestJson))

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
          s"${error.code} is returned from the parser" in new Test {
            val taxYear: String = "2019-20"

            MockCreateAmendUkDividendsAnnualSummaryRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.createAmendUkDividendsAnnualSummary(nino, taxYear)(fakePutRequest(requestJson))

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
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (withPath(ValueFormatError), BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (withPath(RuleIncorrectOrEmptyBodyError), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int, forTaxYear: String): Unit = {

          s"$mtdError is returned from the service for tax year $forTaxYear" in new Test {
            val taxYear: String = forTaxYear

            MockCreateAmendUkDividendsAnnualSummaryRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockCreateAmendAmendUkDividendsAnnualSummaryService
              .createOrAmendAnnualSummary(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.createAmendUkDividendsAnnualSummary(nino, taxYear)(fakePutRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val preTysInput = Seq(
          (NinoFormatError, BAD_REQUEST, "2019-20"),
          (TaxYearFormatError, BAD_REQUEST, "2019-20"),
          (RuleTaxYearNotSupportedError, BAD_REQUEST, "2019-20"),
          (NotFoundError, NOT_FOUND, "2019-20"),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR, "2019-20")
        )

        preTysInput.foreach(args => (serviceErrors _).tupled(args))

        val tysInput = Seq(
          (NinoFormatError, BAD_REQUEST, "2023-24"),
          (TaxYearFormatError, BAD_REQUEST, "2023-24"),
          (RuleTaxYearNotSupportedError, BAD_REQUEST, "2023-24"),
          (NotFoundError, NOT_FOUND, "2023-24"),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR, "2023-24")
        )

        tysInput.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
