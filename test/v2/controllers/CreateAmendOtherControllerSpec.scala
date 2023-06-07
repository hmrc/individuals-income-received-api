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
import api.mocks.services.MockAuditService
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import config.FeatureSwitches
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v2.fixtures.other.CreateAmendOtherFixtures.{postCessationReceiptsItem, requestBodyJson, requestBodyModel, requestBodyWithPCRJson}
import v2.mocks.requestParsers.MockCreateAmendOtherRequestParser
import v2.mocks.services.MockCreateAmendOtherService
import v2.models.request.createAmendOther._
import v2.models.response.createAmendOther.CreateAmendOtherHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendOtherControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateAmendOtherRequestParser
    with MockAuditService
    with MockCreateAmendOtherService
    with MockHateoasFactory
    with MockAppConfig {

  "CreateAmendOtherController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid and postCessationReceipts disabled" in new PrePCRTest {
        MockCreateAmendOtherRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendOtherService
          .createAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAmendOtherHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(hateoasResponse),
          maybeAuditResponseBody = Some(hateoasResponse)
        )
      }

      "the request received is valid and postCessationReceipts enabled" in new PCRTest {
        MockCreateAmendOtherRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendOtherService
          .createAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateAmendOtherHateoasData(nino, taxYear))
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
      "the parser validation fails" in new PrePCRTest {
        MockCreateAmendOtherRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new PrePCRTest {
        MockCreateAmendOtherRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendOtherService
          .createAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {
    val requestJson: JsValue
    val requestBody: CreateAmendOtherRequestBody
    val pCREnabled: Boolean

    val configuration: Configuration = Configuration("postCessationReceipts.enabled" -> pCREnabled)

    implicit val featureSwitch: FeatureSwitches = FeatureSwitches(configuration)

    val taxYear: String = "2019-20"

    val rawData: CreateAmendOtherRawData = CreateAmendOtherRawData(nino, taxYear, AnyContentAsJson(requestJson))

    val requestData: CreateAmendOtherRequest = CreateAmendOtherRequest(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

    val testHateoasLinks: Seq[Link] = Seq(
      Link(href = s"/individuals/income-received/other/$nino/$taxYear", method = PUT, rel = "create-and-amend-other-income"),
      Link(href = s"/individuals/income-received/other/$nino/$taxYear", method = GET, rel = "self"),
      Link(href = s"/individuals/income-received/other/$nino/$taxYear", method = DELETE, rel = "delete-other-income")
    )

    val hateoasResponse: JsValue = Json.parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/income-received/other/AA123456A/$taxYear",
         |         "rel":"create-and-amend-other-income",
         |         "method":"PUT"
         |      },
         |      {
         |         "href":"/individuals/income-received/other/AA123456A/$taxYear",
         |         "rel":"self",
         |         "method":"GET"
         |      },
         |      {
         |         "href":"/individuals/income-received/other/AA123456A/$taxYear",
         |         "rel":"delete-other-income",
         |         "method":"DELETE"
         |      }
         |   ]
         |}
    """.stripMargin
    )

    val controller = new CreateAmendOtherController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateAmendOtherRequestParser,
      service = mockCreateAmendOtherService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      appConfig = mockAppConfig,
      cc = cc,
      idGenerator = mockIdGenerator
    )

//    MockedAppConfig.featureSwitches
//      .returns(configuration)
//      .anyNumberOfTimes()

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendOtherIncome",
        transactionName = "create-amend-other-income",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

    protected def callController(): Future[Result] = controller.createAmendOther(nino, taxYear)(fakePutRequest(requestJson))

  }

  trait PrePCRTest extends Test {
    val requestJson: JsValue                     = requestBodyJson
    val requestBody: CreateAmendOtherRequestBody = requestBodyModel
    val pCREnabled: Boolean                      = false
  }

  trait PCRTest extends Test {
    val requestJson: JsValue                     = requestBodyWithPCRJson
    val requestBody: CreateAmendOtherRequestBody = requestBodyModel.copy(postCessationReceipts = Some(Seq(postCessationReceiptsItem)))
    val pCREnabled: Boolean                      = true
  }

}
