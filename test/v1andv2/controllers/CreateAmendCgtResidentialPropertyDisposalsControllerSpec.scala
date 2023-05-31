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

package v1andv2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.HateoasLinks
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockNrsProxyService}
import api.models.audit.{AuditEvent, AuditResponse}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v1andv2.mocks.requestParsers.MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
import v1andv2.mocks.services._
import v1andv2.models.audit.CreateAmendCgtResidentialPropertyDisposalsAuditDetail
import v1andv2.models.request.createAmendCgtResidentialPropertyDisposals._
import v1andv2.models.response.createAmendCgtResidentialPropertyDisposals.CreateAmendCgtResidentialPropertyDisposalsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendCgtResidentialPropertyDisposalsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockCreateAmendCgtResidentialPropertyDisposalsService
    with MockAuditService
    with MockNrsProxyService
    with MockHateoasFactory
    with MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val taxYear: String = "2020-21"

  val validRequestJson: JsValue = Json.parse(
    """
      |{
      |   "disposals":[
      |      {
      |         "customerReference": "CGTDISPOSAL01",
      |         "disposalDate": "2021-03-24",
      |         "completionDate": "2021-03-26",
      |         "disposalProceeds": 1999.99,
      |         "acquisitionDate": "2021-03-22",
      |         "acquisitionAmount": 1999.99,
      |         "improvementCosts": 1999.99,
      |         "additionalCosts": 1999.99,
      |         "prfAmount": 1999.99,
      |         "otherReliefAmount": 1999.99,
      |         "lossesFromThisYear": 1999.99,
      |         "lossesFromPreviousYear": 1999.99,
      |         "amountOfNetGain": 1999.99,
      |         "amountOfNetLoss": 1999.99
      |      }
      |   ]
      |}
     """.stripMargin
  )

  val rawData: CreateAmendCgtResidentialPropertyDisposalsRawData = CreateAmendCgtResidentialPropertyDisposalsRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson.apply(validRequestJson)
  )

  val requestModel: CreateAmendCgtResidentialPropertyDisposalsRequestBody = CreateAmendCgtResidentialPropertyDisposalsRequestBody(
    disposals = List(
      Disposal(
        customerReference = Some("CGTDISPOSAL01"),
        disposalDate = "2021-03-24",
        completionDate = "2021-03-26",
        disposalProceeds = 1999.99,
        acquisitionDate = "2021-03-22",
        acquisitionAmount = 1999.99,
        improvementCosts = Some(1999.99),
        additionalCosts = Some(1999.99),
        prfAmount = Some(1999.99),
        otherReliefAmount = Some(1999.99),
        lossesFromThisYear = Some(1999.99),
        lossesFromPreviousYear = Some(1999.99),
        amountOfNetGain = Some(1999.99),
        amountOfNetLoss = Some(1999.99)
      )
    )
  )

  val requestData: CreateAmendCgtResidentialPropertyDisposalsRequest = CreateAmendCgtResidentialPropertyDisposalsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestModel
  )

  val mtdResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-cgt-residential-property-disposals"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-cgt-residential-property-disposals"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  val hateoasLinks: List[Link] = List(
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
      method = PUT,
      rel = "create-and-amend-cgt-residential-property-disposals"
    ),
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
      method = GET,
      rel = "self"
    ),
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
      method = DELETE,
      rel = "delete-cgt-residential-property-disposals"
    )
  )

  val auditData: JsValue = Json.parse(s"""
                                         |{
                                         |  "nino":"$nino",
                                         |  "taxYear": "$taxYear"
                                         |  }""".stripMargin)

  "CreateAmendCgtResidentialPropertyDisposalsController" should {
    "return a successful response with status OK" when {
      "happy path" in new Test {
        MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()

        MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockNrsProxyService
          .submitAsync(nino, "itsa-cgt-disposal", validRequestJson)
          .returns(())

        MockCreateAmendCgtResidentialPropertyDisposalsService
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAmendCgtResidentialPropertyDisposalsHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), hateoasLinks))

        runOkTestWithAudit(expectedStatus = OK, Some(mtdResponse), Some(validRequestJson), Some(auditData))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, maybeAuditRequestBody = Some(validRequestJson))
      }

      "service returns an error" in new Test {
        MockCreateAmendCgtResidentialPropertyDisposalsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockNrsProxyService.submitAsync(nino, "itsa-cgt-disposal", validRequestJson)

        MockCreateAmendCgtResidentialPropertyDisposalsService
          .createAndAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(validRequestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[CreateAmendCgtResidentialPropertyDisposalsAuditDetail] {

    val controller = new CreateAmendCgtResidentialPropertyDisposalsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      parser = mockCreateAmendCgtResidentialPropertyDisposalsRequestParser,
      service = mockCreateAmendCgtResidentialPropertyDisposalsService,
      auditService = mockAuditService,
      nrsProxyService = mockNrsProxyService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] =
      controller.createAmendCgtResidentialPropertyDisposals(nino, taxYear)(fakePutRequest(validRequestJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[CreateAmendCgtResidentialPropertyDisposalsAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendCgtResidentialPropertyDisposals",
        transactionName = "Create-Amend-Cgt-Residential-Property-Disposals",
        detail = CreateAmendCgtResidentialPropertyDisposalsAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          nino,
          taxYear,
          requestBody.getOrElse(JsObject.empty),
          correlationId,
          response = auditResponse
        )
      )

    MockedAppConfig.featureSwitches.returns(Configuration("allowTemporalValidationSuspension.enabled" -> true)).anyNumberOfTimes()
  }

}
