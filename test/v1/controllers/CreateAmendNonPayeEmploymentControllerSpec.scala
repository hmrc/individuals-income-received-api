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
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v1.mocks.requestParsers.MockCreateAmendNonPayeEmploymentRequestParser
import v1.mocks.services.MockCreateAmendNonPayeEmploymentService
import v1.models.request.createAmendNonPayeEmployment._
import v1.models.response.createAmendNonPayeEmployment.CreateAmendNonPayeEmploymentHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendNonPayeEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockCreateAmendNonPayeEmploymentService
    with MockAuditService
    with MockCreateAmendNonPayeEmploymentRequestParser
    with MockHateoasFactory {

  val taxYear: String = "2019-20"

  val validRequestJson: JsValue = Json.parse(
    """
      |{
      |    "tips": 100.23
      |}
      |""".stripMargin
  )

  val rawData: CreateAmendNonPayeEmploymentRawData = CreateAmendNonPayeEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson.apply(validRequestJson)
  )

  val requestModel: CreateAmendNonPayeEmploymentRequestBody = CreateAmendNonPayeEmploymentRequestBody(
    tips = 100.23
  )

  val requestData: CreateAmendNonPayeEmploymentRequest = CreateAmendNonPayeEmploymentRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    body = requestModel
  )

  override val testHateoasLinks: Seq[Link] = Seq(
    Link(
      href = s"/individuals/income-received/employments/non-paye/$nino/$taxYear",
      method = PUT,
      rel = "create-and-amend-non-paye-employment-income"),
    Link(href = s"/individuals/income-received/employments/non-paye/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/income-received/employments/non-paye/$nino/$taxYear", method = DELETE, rel = "delete-non-paye-employment-income")
  )

  val mtdResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/employments/non-paye/$nino/$taxYear",
       |         "method":"PUT",
       |         "rel":"create-and-amend-non-paye-employment-income"
       |      },
       |      {
       |         "href":"/individuals/income-received/employments/non-paye/$nino/$taxYear",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/individuals/income-received/employments/non-paye/$nino/$taxYear",
       |         "method":"DELETE",
       |         "rel":"delete-non-paye-employment-income"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  "CreateAmendNonPayeEmploymentController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()

        MockCreateAmendNonPayeEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendNonPayeEmploymentService
          .createAndAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, Unit))))

        MockHateoasFactory
          .wrap((), CreateAmendNonPayeEmploymentHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(validRequestJson),
          maybeExpectedResponseBody = Some(mtdResponse),
          maybeAuditResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockCreateAmendNonPayeEmploymentRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError, Some(validRequestJson))

      }
      "the service returns an error" in new Test {
        MockCreateAmendNonPayeEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendNonPayeEmploymentService
          .createAndAmend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError, Some(validRequestJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking {

    val controller = new CreateAmendNonPayeEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.createAmendNonPayeEmployment(nino, taxYear)(fakePutRequest(validRequestJson))

    def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendNonPayeEmployment",
        transactionName = "create-amend-non-paye-employment",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

    MockedAppConfig.featureSwitches returns Configuration("allowTemporalValidationSuspension.enabled" -> true) anyNumberOfTimes ()
  }

}
