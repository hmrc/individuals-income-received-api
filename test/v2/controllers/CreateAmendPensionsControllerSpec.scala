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
import api.models.audit.{GenericAuditDetail, AuditResponse, AuditEvent}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.{AnyContentAsJson, Result}
import v2.mocks.requestParsers.MockCreateAmendPensionsRequestParser
import v2.mocks.services.MockCreateAmendPensionsService
import v2.models.request.createAmendPensions._
import v2.models.response.createAmendPensions.CreateAndAmendPensionsIncomeHateoasData
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendPensionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAmendPensionsService
    with MockCreateAmendPensionsRequestParser
    with MockHateoasFactory {

  private val taxYear = "2019-20"

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "DEU",
      |         "amountBeforeTax": 100.23,
      |         "taxTakenOff": 1.23,
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      },
      |      {
      |         "countryCode": "FRA",
      |         "amountBeforeTax": 200.25,
      |         "taxTakenOff": 1.27,
      |         "specialWithholdingTax": 2.50,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 3.50
      |      }
      |   ],
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      },
      |      {
      |         "customerReference": "PENSIONINCOME275",
      |         "exemptEmployersPensionContribs": 270.50,
      |         "migrantMemReliefQopsRefNo": "QOPS000245",
      |         "dblTaxationRelief": 5.50,
      |         "dblTaxationCountryCode": "NGA",
      |         "dblTaxationArticle": "AB3477-5",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-1235"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val rawData: CreateAmendPensionsRawData = CreateAmendPensionsRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  private val foreignPensionsItem: List[CreateAmendForeignPensionsItem] = List(
    CreateAmendForeignPensionsItem(
      countryCode = "DEU",
      amountBeforeTax = Some(100.23),
      taxTakenOff = Some(1.23),
      specialWithholdingTax = Some(2.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 3.23
    ),
    CreateAmendForeignPensionsItem(
      countryCode = "FRA",
      amountBeforeTax = Some(200.25),
      taxTakenOff = Some(1.27),
      specialWithholdingTax = Some(2.50),
      foreignTaxCreditRelief = true,
      taxableAmount = 3.50
    )
  )

  private val overseasPensionContributionsItem: List[CreateAmendOverseasPensionContributions] = List(
    CreateAmendOverseasPensionContributions(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 200.23,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(4.23),
      dblTaxationCountryCode = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-123456")
    ),
    CreateAmendOverseasPensionContributions(
      customerReference = Some("PENSIONINCOME275"),
      exemptEmployersPensionContribs = 270.50,
      migrantMemReliefQopsRefNo = Some("QOPS000245"),
      dblTaxationRelief = Some(5.50),
      dblTaxationCountryCode = Some("NGA"),
      dblTaxationArticle = Some("AB3477-5"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-1235")
    )
  )

  private val createAmendPensionsRequestBody: CreateAmendPensionsRequestBody = CreateAmendPensionsRequestBody(
    foreignPensions = Some(foreignPensionsItem),
    overseasPensionContributions = Some(overseasPensionContributionsItem)
  )

  private val requestData: CreateAmendPensionsRequest = CreateAmendPensionsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = createAmendPensionsRequestBody
  )

  private val hateoasLinks = List(
    Link(href = s"/individuals/income-received/pensions/$nino/$taxYear", rel = "create-and-amend-pensions-income", method = PUT),
    Link(href = s"/individuals/income-received/pensions/$nino/$taxYear", rel = "self", method = GET),
    Link(href = s"/individuals/income-received/pensions/$nino/$taxYear", rel = "delete-pensions-income", method = DELETE)
  )

  private val responseBodyJson: JsValue = Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href":"/individuals/income-received/pensions/$nino/$taxYear",
      |         "method":"PUT",
      |         "rel":"create-and-amend-pensions-income"
      |      },
      |      {
      |         "href":"/individuals/income-received/pensions/$nino/$taxYear",
      |         "method":"GET",
      |         "rel":"self"
      |      },
      |      {
      |         "href":"/individuals/income-received/pensions/$nino/$taxYear",
      |         "method":"DELETE",
      |         "rel":"delete-pensions-income"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  "CreateAmendPensionsController" should {
    "return OK" when {
      "the request received is valid" in new Test {
        MockCreateAmendPensionsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendPensionsService
          .createAmendPensions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAndAmendPensionsIncomeHateoasData(nino, taxYear))
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
        MockCreateAmendPensionsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "service returns an error" in new Test {
        MockCreateAmendPensionsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendPensionsService
          .createAmendPensions(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new CreateAmendPensionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendPensionsRequestParser,
      service = mockCreateAmendPensionsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator,
      hateoasFactory = mockHateoasFactory
    )

    protected def callController(): Future[Result] = controller.createAmendPensions(nino, taxYear)(fakePutRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendPensionsIncome",
        transactionName = "create-amend-pensions-income",
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
