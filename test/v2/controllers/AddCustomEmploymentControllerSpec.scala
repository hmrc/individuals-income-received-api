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
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas
import api.models.hateoas.HateoasWrapper
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import v2.mocks.requestParsers.MockAddCustomEmploymentRequestParser
import v2.mocks.services.MockAddCustomEmploymentService
import v2.models.request.addCustomEmployment.{AddCustomEmploymentRawData, AddCustomEmploymentRequest, AddCustomEmploymentRequestBody}
import v2.models.response.addCustomEmployment.{AddCustomEmploymentHateoasData, AddCustomEmploymentResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddCustomEmploymentControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockAddCustomEmploymentService
    with MockAuditService
    with MockAddCustomEmploymentRequestParser
    with MockHateoasFactory {

  val taxYear: String      = "2019-20"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "employerRef": "123/AZ12334",
      |  "employerName": "AMD infotech Ltd",
      |  "startDate": "2019-01-01",
      |  "cessationDate": "2020-06-01",
      |  "payrollId": "124214112412",
      |  "occupationalPension": false
      |}
    """.stripMargin
  )

  val rawData: AddCustomEmploymentRawData = AddCustomEmploymentRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val addCustomEmploymentRequestBody: AddCustomEmploymentRequestBody = AddCustomEmploymentRequestBody(
    employerRef = Some("123/AZ12334"),
    employerName = "AMD infotech Ltd",
    startDate = "2019-01-01",
    cessationDate = Some("2020-06-01"),
    payrollId = Some("124214112412"),
    occupationalPension = false
  )

  val requestData: AddCustomEmploymentRequest = AddCustomEmploymentRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = addCustomEmploymentRequestBody
  )

  val responseData: AddCustomEmploymentResponse = AddCustomEmploymentResponse(
    employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  private val links = Seq(
    hateoas.Link(href = s"/individuals/income-received/employments/$nino/$taxYear", method = GET, rel = "list-employments"),
    hateoas.Link(href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId", method = GET, rel = "self"),
    hateoas.Link(href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId", method = PUT, rel = "amend-custom-employment"),
    hateoas.Link(href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId", method = DELETE, rel = "delete-custom-employment")
  )

  val responseJson: JsValue = Json.parse(
    s"""
       |{
       |   "employmentId": "$employmentId",
       |   "links":[
       |      {
       |         "href": "/individuals/income-received/employments/$nino/$taxYear",
       |         "rel": "list-employments",
       |         "method": "GET"
       |      },
       |      {
       |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |         "rel": "self",
       |         "method": "GET"
       |      },
       |      {
       |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |         "rel": "amend-custom-employment",
       |         "method": "PUT"
       |      },
       |      {
       |         "href": "/individuals/income-received/employments/$nino/$taxYear/$employmentId",
       |         "rel": "delete-custom-employment",
       |         "method": "DELETE"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  "AddCustomEmploymentController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        MockAddCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAddCustomEmploymentService
          .addEmployment(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, AddCustomEmploymentHateoasData(nino, taxYear, employmentId))
          .returns(HateoasWrapper(responseData, links))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeAuditRequestBody = Some(requestBodyJson),
          maybeExpectedResponseBody = Some(responseJson),
          maybeAuditResponseBody = Some(responseJson)
        )
      }
    }

    "return the error as per spec" when {
      "parser validation fails" in new Test {
        MockAddCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError, Some(requestBodyJson))
      }

      "the service returns an error" in new Test {
        MockAddCustomEmploymentRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAddCustomEmploymentService
          .addEmployment(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))))

        runErrorTestWithAudit(RuleTaxYearNotEndedError, Some(requestBodyJson))
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new AddCustomEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      parser = mockAddCustomEmploymentRequestParser,
      service = mockAddCustomEmploymentService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.addEmployment(nino, taxYear)(fakePostRequest(requestBodyJson))

    MockedAppConfig.featureSwitches.returns(Configuration("allowTemporalValidationSuspension.enabled" -> true)).anyNumberOfTimes()

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "AddACustomEmployment",
        transactionName = "add-a-custom-employment",
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
