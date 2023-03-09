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

import api.controllers.ControllerBaseSpec
import api.mocks.MockIdGenerator
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.other.CreateAmendOtherFixtures.{requestBodyJson, requestBodyModel, responseWithHateoasLinks}
import v1.mocks.requestParsers.MockCreateAmendOtherRequestParser
import v1.mocks.services.MockCreateAmendOtherService
import v1.models.request.createAmendOther._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendOtherControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockCreateAmendOtherRequestParser
    with MockAuditService
    with MockCreateAmendOtherService
    with MockIdGenerator {

  val nino: String          = "AA123456A"
  val taxYear: String       = "2019-20"
  val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAmendOtherController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockCreateAmendOtherRequestParser,
      service = mockCreateAmendOtherService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  val rawData: CreateAmendOtherRawData = CreateAmendOtherRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val requestData: CreateAmendOtherRequest = CreateAmendOtherRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestBodyModel
  )

  def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateAmendOtherIncome",
      transactionName = "create-amend-other-income",
      detail = GenericAuditDetail(
        "Individual",
        None,
        Map("nino" -> nino, "taxYear" -> taxYear),
        None,
        Some(requestBodyJson),
        correlationId,
        auditResponse
      )
    )

  "CreateAmendOtherController" should {
    "return OK" when {
      "happy path" in new Test {

        MockCreateAmendOtherRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendOtherService
          .createAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.createAmendOther(nino, taxYear)(fakePutRequest(requestBodyJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseWithHateoasLinks(taxYear)
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(responseWithHateoasLinks(taxYear)))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateAmendOtherRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.createAmendOther(nino, taxYear)(fakePutRequest(requestBodyJson))

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
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (CountryCodeFormatError, BAD_REQUEST),
          (CountryCodeRuleError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateAmendOtherRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockCreateAmendOtherService
              .createAmend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.createAmendOther(nino, taxYear)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val errors = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (InternalError, INTERNAL_SERVER_ERROR)
        )

        val extraTysErrors = List(
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
