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
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.HateoasWrapper
import api.models.hateoas.RelType.CREATE_AND_AMEND_UK_SAVINGS
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.{AnyContentAsJson, Result}
import v1andv2.mocks.requestParsers.MockCreateAmendUkSavingsAnnualSummaryRequestParser
import v1andv2.mocks.services.MockCreateAmendUkSavingsAnnualSummaryService
import v1andv2.models.request.createAmendUkSavingsAnnualSummary._
import v1andv2.models.response.createAmendUkSavingsIncomeAnnualSummary.CreateAndAmendUkSavingsAnnualSummaryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendUkSavingsAnnualSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAmendUkSavingsAnnualSummaryService
    with MockCreateAmendUkSavingsAnnualSummaryRequestParser
    with MockAuditService
    with MockHateoasFactory {

  val taxYear: String          = "2019-20"
  val savingsAccountId: String = "acctId"
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

  "CreateAmendUkSavingsAnnualSummaryController" should {
    "return OK" when {
      "happy path" in new Test {
        MockCreateAmendUkSavingsAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendAmendUkSavingsAnnualSummaryService
          .createOrAmendAnnualSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAndAmendUkSavingsAnnualSummaryHateoasData(nino, taxYear, savingsAccountId))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(testHateoasLinksJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockCreateAmendUkSavingsAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockCreateAmendUkSavingsAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendAmendUkSavingsAnnualSummaryService
          .createOrAmendAnnualSummary(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    val controller = new CreateAmendUkSavingsAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendUkSavingsAnnualSummaryRequestParser,
      service = mockCreateAmendUkSavingsAnnualSummaryService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] =
      controller.createAmendUkSavingsAnnualSummary(nino, taxYear, savingsAccountId)(fakePostRequest(requestJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] = {
      AuditEvent(
        auditType = "createAmendUkSavingsAnnualSummary",
        transactionName = CREATE_AND_AMEND_UK_SAVINGS,
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some("1.0"),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino, "taxYear" -> taxYear, "savingsAccountId" -> savingsAccountId),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )
    }

  }

}
