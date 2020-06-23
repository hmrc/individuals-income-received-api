/*
 * Copyright 2020 HM Revenue & Customs
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

import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.domain.Nino
import v1.mocks.requestParsers.MockAmendInsurancePoliciesRequestParser
import v1.mocks.services.{MockAmendInsurancePoliciesService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.domain.DesTaxYear
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendInsurancePolicies.{AmendCommonInsurancePoliciesItem, AmendForeignPoliciesItem, AmendInsurancePoliciesRawData, AmendInsurancePoliciesRequest, AmendInsurancePoliciesRequestBody, AmendVoidedIsaPoliciesItem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendInsurancePoliciesControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAmendInsurancePoliciesService
    with MockAmendInsurancePoliciesRequestParser{

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AmendInsurancePoliciesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendInsurancePoliciesRequestParser,
      service = mockAmendInsurancePoliciesService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockedAppConfig.apiGatewayContext.returns("baseUrl").anyNumberOfTimes()
  }

  val nino: String = "AA123456A"
  val taxYear: String = "2017-18"
  val correlationId: String = "X-123"

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "lifeInsurance":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "capitalRedemption":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "lifeAnnuity":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaid": true,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12,
      |           "deficiencyRelief": 5000.99
      |       }
      |   ],
      |   "voidedIsa":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "event": "Death of spouse",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15,
      |           "yearsHeldSinceLastGain": 12
      |       }
      |   ],
      |   "foreign":[
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       },
      |       {
      |           "customerReference": "INPOLY123A",
      |           "gainAmount": 2000.99,
      |           "taxPaidAmount": 5000.99,
      |           "yearsHeld": 15
      |       }
      |   ]
      |}
    """.stripMargin
  )

  val rawData: AmendInsurancePoliciesRawData = AmendInsurancePoliciesRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val lifeInsurance: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = "INPOLY123A",
      event = Some("Death of spouse"),
      gainAmount = Some(2000.99),
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = "INPOLY123A",
      event = Some("Death of spouse"),
      gainAmount = Some(2000.99),
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val capitalRedemption: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = "INPOLY123A",
      event = Some("Death of spouse"),
      gainAmount = Some(2000.99),
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = "INPOLY123A",
      event = Some("Death of spouse"),
      gainAmount = Some(2000.99),
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )

  val lifeAnnuity: Seq[AmendCommonInsurancePoliciesItem] = Seq(
    AmendCommonInsurancePoliciesItem(
      customerReference = "INPOLY123A",
      event = Some("Death of spouse"),
      gainAmount = Some(2000.99),
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    ),
    AmendCommonInsurancePoliciesItem(
      customerReference = "INPOLY123A",
      event = Some("Death of spouse"),
      gainAmount = Some(2000.99),
      taxPaid = true,
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12),
      deficiencyRelief = Some(5000.99)
    )
  )
  val voidedIsa: Seq[AmendVoidedIsaPoliciesItem] = Seq(
    AmendVoidedIsaPoliciesItem(
      customerReference = "INPOLY123A",
      event = Some("Death of spouse"),
      gainAmount = Some(2000.99),
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12)
    ),
    AmendVoidedIsaPoliciesItem(
      customerReference = "INPOLY123A",
      event = Some("Death of spouse"),
      gainAmount = Some(2000.99),
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15),
      yearsHeldSinceLastGain = Some(12)
    )
  )

  val foreign: Seq[AmendForeignPoliciesItem] = Seq(
    AmendForeignPoliciesItem(
      customerReference = "INPOLY123A",
      gainAmount = Some(2000.99),
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15)
    ),
    AmendForeignPoliciesItem(
      customerReference = "INPOLY123A",
      gainAmount = Some(2000.99),
      taxPaidAmount = Some(5000.99),
      yearsHeld = Some(15)
    )
  )

  val amendInsurancePoliciesRequestBody: AmendInsurancePoliciesRequestBody = AmendInsurancePoliciesRequestBody(
    lifeInsurance = Some(lifeInsurance),
    capitalRedemption = Some(capitalRedemption),
    lifeAnnuity = Some(lifeAnnuity),
    voidedIsa = Some(voidedIsa),
    foreign = Some(foreign)
  )

  val requestData: AmendInsurancePoliciesRequest = AmendInsurancePoliciesRequest(
    nino = Nino(nino),
    taxYear = DesTaxYear.fromMtd(taxYear),
    body = amendInsurancePoliciesRequestBody
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href":"/baseUrl/insurance-policies/$nino/$taxYear",
      |         "rel":"amend-insurance-policies-income",
      |         "method":"PUT"
      |      },
      |      {
      |         "href":"/baseUrl/insurance-policies/$nino/$taxYear",
      |         "rel":"self",
      |         "method":"GET"
      |      },
      |      {
      |         "href":"/baseUrl/insurance-policies/$nino/$taxYear",
      |         "rel":"delete-insurance-policies-income",
      |         "method":"DELETE"
      |      }
      |   ]
      |}
      |""".stripMargin
  )

  "AmendInsurancePoliciesController" should {
    "return NO_content" when {
      "happy path" in new Test {

        MockAmendInsurancePoliciesRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendInsurancePoliciesService
          .amendInsurancePolicies(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendInsurancePolicies(nino, taxYear)(fakePutRequest(requestBodyJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendInsurancePoliciesRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.amendInsurancePolicies(nino, taxYear)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (CustomerRefFormatError, BAD_REQUEST),
          (EventFormatError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendInsurancePoliciesRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendInsurancePoliciesService
              .amendInsurancePolicies(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.amendInsurancePolicies(nino, taxYear)(fakePutRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
