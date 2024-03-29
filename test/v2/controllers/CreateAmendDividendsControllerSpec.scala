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
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v2.mocks.requestParsers.MockCreateAmendDividendsRequestParser
import v2.mocks.services.MockCreateAmendDividendsService
import v2.models.request.createAmendDividends._
import v2.models.response.createAmendDividends.CreateAmendDividendsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendDividendsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAuditService
    with MockCreateAmendDividendsService
    with MockCreateAmendDividendsRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  val taxYear: String = "2019-20"

  val validRequestJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |      }
      |   ],
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      },
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 2500.99
      |       }
      |   ],
      |   "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": 12321.22
      |   },
      |   "redeemableShares": {
      |      "customerReference": "my shares",
      |      "grossAmount": 12345.75
      |   },
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "my secs",
      |      "grossAmount": 12500.89
      |   },
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off",
      |      "grossAmount": 13700.55
      |   }
      |}
    """.stripMargin
  )

  val rawData: CreateAmendDividendsRawData = CreateAmendDividendsRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(validRequestJson)
  )

  val foreignDividend: List[CreateAmendForeignDividendItem] = List(
    CreateAmendForeignDividendItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = Some(true),
      taxableAmount = 2321.22
    ),
    CreateAmendForeignDividendItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = Some(false),
      taxableAmount = 2500.99
    )
  )

  val dividendIncomeReceivedWhilstAbroad: List[CreateAmendDividendIncomeReceivedWhilstAbroadItem] = List(
    CreateAmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "DEU",
      amountBeforeTax = Some(1232.22),
      taxTakenOff = Some(22.22),
      specialWithholdingTax = Some(27.35),
      foreignTaxCreditRelief = Some(true),
      taxableAmount = 2321.22
    ),
    CreateAmendDividendIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1350.55),
      taxTakenOff = Some(25.27),
      specialWithholdingTax = Some(30.59),
      foreignTaxCreditRelief = Some(false),
      taxableAmount = 2500.99
    )
  )

  val stockDividend: CreateAmendCommonDividends = CreateAmendCommonDividends(
    customerReference = Some("my divs"),
    grossAmount = 12321.22
  )

  val redeemableShares: CreateAmendCommonDividends = CreateAmendCommonDividends(
    customerReference = Some("my shares"),
    grossAmount = 12345.75
  )

  val bonusIssuesOfSecurities: CreateAmendCommonDividends = CreateAmendCommonDividends(
    customerReference = Some("my secs"),
    grossAmount = 12500.89
  )

  val closeCompanyLoansWrittenOff: CreateAmendCommonDividends = CreateAmendCommonDividends(
    customerReference = Some("write off"),
    grossAmount = 13700.55
  )

  val createAmendDividendsRequestBody: CreateAmendDividendsRequestBody = CreateAmendDividendsRequestBody(
    foreignDividend = Some(foreignDividend),
    dividendIncomeReceivedWhilstAbroad = Some(dividendIncomeReceivedWhilstAbroad),
    stockDividend = Some(stockDividend),
    redeemableShares = Some(redeemableShares),
    bonusIssuesOfSecurities = Some(bonusIssuesOfSecurities),
    closeCompanyLoansWrittenOff = Some(closeCompanyLoansWrittenOff)
  )

  val requestData: CreateAmendDividendsRequest = CreateAmendDividendsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = createAmendDividendsRequestBody
  )

  val mtdResponse: JsValue = Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
      |         "method":"PUT",
      |         "rel":"create-and-amend-dividends-income"
      |      },
      |      {
      |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
      |         "method":"GET",
      |         "rel":"self"
      |      },
      |      {
      |         "href":"/individuals/income-received/dividends/$nino/$taxYear",
      |         "method":"DELETE",
      |         "rel":"delete-dividends-income"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val hateoasLinks: List[Link] = List(
    Link(
      href = s"/individuals/income-received/dividends/$nino/$taxYear",
      method = PUT,
      rel = "create-and-amend-dividends-income"
    ),
    Link(
      href = s"/individuals/income-received/dividends/$nino/$taxYear",
      method = GET,
      rel = "self"
    ),
    Link(
      href = s"/individuals/income-received/dividends/$nino/$taxYear",
      method = DELETE,
      rel = "delete-dividends-income"
    )
  )

  val auditData: JsValue = Json.parse(s"""
                                         |{
                                         |  "nino":"$nino",
                                         |  "taxYear": "$taxYear"
                                         |  }""".stripMargin)

  "CreateAmendDividendsController" should {
    "return a successful response with status OK" when {
      "happy path" in new Test {
        MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()

        MockCreateAmendDividendsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendDividendsService
          .createAmendDividends(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAmendDividendsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), hateoasLinks))

        runOkTestWithAudit(expectedStatus = OK, Some(mtdResponse), Some(validRequestJson), Some(auditData))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockCreateAmendDividendsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, maybeAuditRequestBody = Some(validRequestJson))
      }

      "service returns an error" in new Test {
        MockCreateAmendDividendsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendDividendsService
          .createAmendDividends(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(validRequestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new CreateAmendDividendsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      parser = mockCreateAmendDividendsRequestParser,
      service = mockCreateAmendDividendsService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.createAmendDividends(nino, taxYear)(fakePutRequest(validRequestJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendDividendsIncome",
        transactionName = "create-amend-dividends-income",
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
