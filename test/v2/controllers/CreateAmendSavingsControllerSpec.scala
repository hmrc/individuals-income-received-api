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

package v2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.hateoas.MockHateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v2.mocks.requestParsers.MockCreateCreateAmendSavingsRequestParser
import v2.mocks.services.MockCreateAmendSavingsService
import v2.models.request.amendSavings._
import v2.models.response.createAmendSavings.CreateAndAmendSavingsIncomeHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendSavingsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAmendSavingsService
    with MockCreateCreateAmendSavingsRequestParser
    with MockHateoasFactory {

  private val taxYear = "2019-20"

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "securities":
      |      {
      |        "taxTakenOff": 100.11,
      |        "grossAmount": 100.22,
      |        "netAmount": 100.33
      |      },
      |  "foreignInterest":   [
      |     {
      |        "amountBeforeTax": 101.11,
      |        "countryCode": "FRA",
      |        "taxTakenOff": 102.22,
      |        "specialWithholdingTax": 103.33,
      |        "taxableAmount": 104.44,
      |        "foreignTaxCreditRelief": true
      |      },
      |      {
      |        "amountBeforeTax": 201.11,
      |        "countryCode": "DEU",
      |        "taxTakenOff": 202.22,
      |        "specialWithholdingTax": 203.33,
      |        "taxableAmount": 204.44,
      |        "foreignTaxCreditRelief": true
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val rawData: CreateAmendSavingsRawData = CreateAmendSavingsRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  private val security: AmendSecurities = AmendSecurities(
    taxTakenOff = Some(100.11),
    grossAmount = 200.22,
    netAmount = Some(300.33)
  )

  private val foreignInterests: List[AmendForeignInterestItem] = List(
    AmendForeignInterestItem(
      amountBeforeTax = Some(101.11),
      countryCode = "FRA",
      taxTakenOff = Some(102.22),
      specialWithholdingTax = Some(103.33),
      taxableAmount = 104.44,
      foreignTaxCreditRelief = Some(false)
    ),
    AmendForeignInterestItem(
      amountBeforeTax = Some(201.11),
      countryCode = "GER",
      taxTakenOff = Some(202.22),
      specialWithholdingTax = Some(203.33),
      taxableAmount = 204.44,
      foreignTaxCreditRelief = Some(false)
    )
  )

  private val amendSavingsRequestBody: CreateAmendSavingsRequestBody = CreateAmendSavingsRequestBody(
    securities = Some(security),
    foreignInterest = Some(foreignInterests)
  )

  private val requestData: CreateAmendSavingsRequest = CreateAmendSavingsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = amendSavingsRequestBody
  )

  private val hateoasLinks = List(
    Link(href = s"/individuals/income-received/savings/$nino/$taxYear", method = PUT, rel = "create-and-amend-savings-income"),
    Link(href = s"/individuals/income-received/savings/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/income-received/savings/$nino/$taxYear", method = DELETE, rel = "delete-savings-income")
  )

  private val responseBodyJson: JsValue = Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href":"/individuals/income-received/savings/$nino/$taxYear",
      |         "rel":"create-and-amend-savings-income",
      |         "method":"PUT"
      |      },
      |      {
      |         "href":"/individuals/income-received/savings/$nino/$taxYear",
      |         "rel":"self",
      |         "method":"GET"
      |      },
      |      {
      |         "href":"/individuals/income-received/savings/$nino/$taxYear",
      |         "rel":"delete-savings-income",
      |         "method":"DELETE"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  "CreateAmendSavingsController" should {
    "return OK" when {
      "the request received is valid" in new Test {
        MockCreateAmendSavingsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendSavingsService
          .createAmendSaving(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAndAmendSavingsIncomeHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), hateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(responseBodyJson),
          maybeAuditResponseBody = Some(responseBodyJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockCreateAmendSavingsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        MockCreateAmendSavingsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendSavingsService
          .createAmendSaving(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new CreateAmendSavingsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendSavingsRequestParser,
      service = mockCreateAmendSavingsService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.createAmendSaving(nino, taxYear)(fakePutRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendSavingsIncome",
        transactionName = "create-amend-savings-income",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

}
