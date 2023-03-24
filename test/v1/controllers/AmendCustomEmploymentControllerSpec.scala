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
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v1.mocks.requestParsers.MockAmendCustomEmploymentRequestParser
import v1.mocks.services.MockAmendCustomEmploymentService
import v1.models.request.amendCustomEmployment._
import v1.models.response.amendCustomEmployment.AmendCustomEmploymentHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendCustomEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockAuditService
    with MockAmendCustomEmploymentRequestParser
    with MockAmendCustomEmploymentService
    with MockHateoasFactory {

  val taxYear: String = "2019-20"
  val employmentId    = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412"
      |}
    """.stripMargin
  )

  val rawData: AmendCustomEmploymentRawData = AmendCustomEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId,
    body = AnyContentAsJson(requestBodyJson)
  )

  val amendCustomEmploymentRequestBody: AmendCustomEmploymentRequestBody = AmendCustomEmploymentRequestBody(
    employerRef = Some("123/AZ12334"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412"),
    occupationalPension = false
  )

  val requestData: AmendCustomEmploymentRequest = AmendCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    employmentId = employmentId,
    body = amendCustomEmploymentRequestBody
  )

  override val testHateoasLinks: Seq[Link] = Seq(
    hateoas.Link(href = s"/individuals/income-received/employments/$nino/$taxYear", method = GET, rel = "list-employments"),
    hateoas.Link(href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId", method = GET, rel = "self"),
    hateoas.Link(href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId", method = PUT, rel = "amend-custom-employment"),
    hateoas.Link(href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId", method = DELETE, rel = "delete-custom-employment")
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/individuals/income-received/employments/$nino/$taxYear",
       |         "rel":"list-employments",
       |         "method":"GET"
       |      },
       |      {
       |         "href":"/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |         "rel":"self",
       |         "method":"GET"
       |      },
       |      {
       |         "href":"/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |         "rel":"amend-custom-employment",
       |         "method":"PUT"
       |      },
       |      {
       |         "href":"/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |         "rel":"delete-custom-employment",
       |         "method":"DELETE"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  "AmendCustomEmploymentController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        MockAmendCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendCustomEmploymentService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendCustomEmploymentHateoasData(nino, taxYear, employmentId))
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
        MockAmendCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        MockAmendCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendCustomEmploymentService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))))

        runErrorTestWithAudit(RuleTaxYearNotEndedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new AmendCustomEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      parser = mockAmendCustomEmploymentRequestParser,
      service = mockAmendCustomEmploymentService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.returns(Configuration("allowTemporalValidationSuspension.enabled" -> true)).anyNumberOfTimes()

    protected def callController(): Future[Result] = controller.amendEmployment(nino, taxYear, employmentId)(fakePutRequest(requestBodyJson))

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AmendACustomEmployment",
        transactionName = "amend-a-custom-employment",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          request = requestBody,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

}
