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
import api.models.hateoas
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v1.mocks.requestParsers.MockAmendForeignRequestParser
import v1.mocks.services.MockAmendForeignService
import v1.models.request.amendForeign._
import v1.models.response.amendForeign.AmendForeignHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockAuditService
    with MockAmendForeignService
    with MockAmendForeignRequestParser
    with MockHateoasFactory {

  val taxYear: String = "2019-20"

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignEarnings": {
      |      "customerReference": "FOREIGNINCME123A",
      |      "earningsNotTaxableUK": 1999.99
      |   },
      |   "unremittableForeignIncome": [
      |       {
      |          "countryCode": "FRA",
      |          "amountInForeignCurrency": 1999.99,
      |          "amountTaxPaid": 1999.99
      |       },
      |       {
      |          "countryCode": "IND",
      |          "amountInForeignCurrency": 2999.99,
      |          "amountTaxPaid": 2999.99
      |       }
      |    ]
      |}
    """.stripMargin
  )

  val rawData: AmendForeignRawData = AmendForeignRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val foreignEarning: ForeignEarnings = ForeignEarnings(
    customerReference = Some("FOREIGNINCME123A"),
    earningsNotTaxableUK = 1999.99
  )

  val unremittableForeignIncomeItems: Seq[UnremittableForeignIncomeItem] = Seq(
    UnremittableForeignIncomeItem(
      countryCode = "FRA",
      amountInForeignCurrency = 1999.99,
      amountTaxPaid = Some(1999.99)
    ),
    UnremittableForeignIncomeItem(
      countryCode = "IND",
      amountInForeignCurrency = 2999.99,
      amountTaxPaid = Some(2999.99)
    )
  )

  val amendForeignRequestBody: AmendForeignRequestBody = AmendForeignRequestBody(
    foreignEarnings = Some(foreignEarning),
    unremittableForeignIncome = Some(unremittableForeignIncomeItems)
  )

  val requestData: AmendForeignRequest = AmendForeignRequest(nino = Nino(nino), taxYear = TaxYear.fromMtd(taxYear), body = amendForeignRequestBody)

  override val testHateoasLinks: Seq[Link] = Seq(
    hateoas.Link(href = s"/individuals/income-received/foreign/$nino/$taxYear", method = PUT, rel = "create-and-amend-foreign-income"),
    hateoas.Link(href = s"/individuals/income-received/foreign/$nino/$taxYear", method = GET, rel = "self"),
    hateoas.Link(href = s"/individuals/income-received/foreign/$nino/$taxYear", method = DELETE, rel = "delete-foreign-income")
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href":"/individuals/income-received/foreign/$nino/$taxYear",
      |         "rel":"create-and-amend-foreign-income",
      |         "method":"PUT"
      |      },
      |      {
      |         "href":"/individuals/income-received/foreign/$nino/$taxYear",
      |         "rel":"self",
      |         "method":"GET"
      |      },
      |      {
      |         "href":"/individuals/income-received/foreign/$nino/$taxYear",
      |         "rel":"delete-foreign-income",
      |         "method":"DELETE"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  "AmendForeignController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        MockAmendForeignRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendForeignService
          .amendForeign(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendForeignHateoasData(nino, taxYear))
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
        MockAmendForeignRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        MockAmendForeignRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendForeignService
          .amendForeign(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    val controller = new AmendForeignController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendForeignRequestParser,
      service = mockAmendForeignService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.amendForeign(nino, taxYear)(fakePutRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendForeignIncome",
        transactionName = "create-amend-foreign-income",
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
