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
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
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
    with ControllerTestRunner
    with MockCreateAmendUkDividendsAnnualSummaryService
    with MockCreateAmendUkDividendsAnnualSummaryRequestParser
    with MockHateoasFactory {

  val taxYear: String = "2019-20"
  val mtdId: String   = "test-mtd-id"

  val requestJson: JsObject = JsObject.empty

  val rawData: CreateAmendUkDividendsIncomeAnnualSummaryRawData = CreateAmendUkDividendsIncomeAnnualSummaryRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson.apply(requestJson)
  )

  val requestModel: CreateAmendUkDividendsIncomeAnnualSummaryBody = CreateAmendUkDividendsIncomeAnnualSummaryBody(None, None)

  val requestData: CreateAmendUkDividendsIncomeAnnualSummaryRequest = CreateAmendUkDividendsIncomeAnnualSummaryRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestModel
  )

  private val hateoasLinks = Seq(
    Link(href = s"/individuals/income-received/uk-dividends/$nino/$taxYear", method = PUT, rel = "create-and-amend-uk-dividends-income"),
    Link(href = s"/individuals/income-received/uk-dividends/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/income-received/uk-dividends/$nino/$taxYear", method = DELETE, rel = "delete-uk-dividends-income")
  )

  val responseBodyJson: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/uk-dividends/$nino/$taxYear",
       |         "rel":"create-and-amend-uk-dividends-income",
       |         "method":"PUT"
       |      },
       |      {
       |         "href":"/individuals/income-received/uk-dividends/$nino/$taxYear",
       |         "rel":"self",
       |         "method":"GET"
       |      },
       |      {
       |         "href":"/individuals/income-received/uk-dividends/$nino/$taxYear",
       |         "rel":"delete-uk-dividends-income",
       |         "method":"DELETE"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  "CreateAmendUkDividendsAnnualSummaryController" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {

        MockCreateAmendUkDividendsAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendAmendUkDividendsAnnualSummaryService
          .createOrAmendAnnualSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAndAmendUkDividendsIncomeAnnualSummaryHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), hateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBodyJson))

      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        MockCreateAmendUkDividendsAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {

        MockCreateAmendUkDividendsAnnualSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendAmendUkDividendsAnnualSummaryService
          .createOrAmendAnnualSummary(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    val controller = new CreateAmendUkDividendsAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendUkDividendsAnnualSummaryRequestParser,
      service = mockCreateAmendUkDividendsAnnualSummaryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator,
      auditService = mockAuditService
    )

    protected def callController(): Future[Result] = controller.createAmendUkDividendsAnnualSummary(nino, taxYear)(fakePutRequest(requestJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
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

}
