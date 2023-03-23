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
import v1.mocks.requestParsers.MockCreateAmendCgtPpdOverridesRequestParser
import v1.mocks.services._
import v1.models.audit.CreateAmendCgtPpdOverridesAuditDetail
import v1.models.request.createAmendCgtPpdOverrides._
import v1.models.response.createAmendCgtPpdOverrides.CreateAmendCgtPpdOverridesHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendCgtPpdOverridesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockCreateAmendCgtPpdOverridesService
    with MockAuditService
    with MockNrsProxyService
    with MockHateoasFactory
    with MockCreateAmendCgtPpdOverridesRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val taxYear: String = "2019-20"

  val validRequestJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "ppdSubmissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.78
      |         },
      |         {
      |            "ppdSubmissionId": "AB0000000098",
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "ppdSubmissionId": "AB0000000098",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23,
      |             "amountOfNetGain": 4567.89
      |         },
      |         {
      |             "ppdSubmissionId": "AB0000000091",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23,
      |             "amountOfNetLoss": 4567.89
      |         }
      |    ]
      |}
      |""".stripMargin
  )

  val rawData: CreateAmendCgtPpdOverridesRawData = CreateAmendCgtPpdOverridesRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson.apply(validRequestJson)
  )

  //@formatter:off
  val requestModel: CreateAmendCgtPpdOverridesRequestBody = CreateAmendCgtPpdOverridesRequestBody(
    multiplePropertyDisposals = Some(
      List(
        MultiplePropertyDisposals("AB0000000092", Some(1234.78), None),
        MultiplePropertyDisposals("AB0000000098", None, Some(134.99))
      )),
    singlePropertyDisposals = Some(
      List(
        SinglePropertyDisposals("AB0000000098", "2020-02-28", 454.24, Some("2020-03-29"), 3434.45, 233.45,
          423.34, 2324.67, 3434.23, Some(436.23), Some(234.23), Some(4567.89), None),
        SinglePropertyDisposals("AB0000000091", "2020-02-28", 454.24, Some("2020-03-29"), 3434.45, 233.45,
          423.34, 2324.67, 3434.23, Some(436.23), Some(234.23), None, Some(4567.89)
        )
      ))
  )
  //@formatter:on

  val requestData: CreateAmendCgtPpdOverridesRequest = CreateAmendCgtPpdOverridesRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestModel
  )

  val mtdResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
       |         "method":"PUT",
       |         "rel":"create-and-amend-report-and-pay-capital-gains-tax-on-property-overrides"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
       |         "method":"DELETE",
       |         "rel":"delete-report-and-pay-capital-gains-tax-on-property-overrides"
       |      },
       |      {
       |         "href":"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  val hateoasLinks: List[Link] = List(
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
      method = PUT,
      rel = "create-and-amend-report-and-pay-capital-gains-tax-on-property-overrides"
    ),
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
      method = DELETE,
      rel = "delete-report-and-pay-capital-gains-tax-on-property-overrides"
    ),
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
      method = GET,
      rel = "self"
    )
  )

  val auditData: JsValue = Json.parse(s"""
                                         |{
                                         |  "nino":"$nino",
                                         |  "taxYear": "$taxYear"
                                         |  }""".stripMargin)

  "CreateAmendCgtPpdOverridesController" should {
    "return a successful response with status OK" when {
      "happy path" in new Test {
        MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()

        MockCreateAmendCgtPpdOverridesRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockNrsProxyService
          .submitAsync(nino, "itsa-cgt-disposal-ppd", validRequestJson)
          .returns(())

        MockCreateAmendCgtPpdOverridesService
          .createAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAmendCgtPpdOverridesHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), hateoasLinks))

        runOkTestWithAudit(expectedStatus = OK, Some(mtdResponse), Some(validRequestJson), Some(auditData))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockCreateAmendCgtPpdOverridesRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, maybeAuditRequestBody = Some(validRequestJson))
      }

      "service returns an error" in new Test {
        MockCreateAmendCgtPpdOverridesRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockNrsProxyService
          .submitAsync(nino, "itsa-cgt-disposal-ppd", validRequestJson)
          .returns(())

        MockCreateAmendCgtPpdOverridesService
          .createAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, maybeAuditRequestBody = Some(validRequestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[CreateAmendCgtPpdOverridesAuditDetail] {

    val controller = new CreateAmendCgtPpdOverridesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      parser = mockCreateAmendCgtPpdOverridesRequestParser,
      service = mockCreateAmendCgtPpdOverridesService,
      auditService = mockAuditService,
      nrsProxyService = mockNrsProxyService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.createAmendCgtPpdOverrides(nino, taxYear)(fakePutRequest(validRequestJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[CreateAmendCgtPpdOverridesAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendCgtPpdOverrides",
        transactionName = "Create-Amend-Cgt-Ppd-Overrides",
        detail = CreateAmendCgtPpdOverridesAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          nino,
          taxYear,
          requestBody.getOrElse(JsObject.empty),
          correlationId,
          response = auditResponse
        )
      )

    MockedAppConfig.featureSwitches returns Configuration("allowTemporalValidationSuspension.enabled" -> true) anyNumberOfTimes ()
  }

}
