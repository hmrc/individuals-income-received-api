/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockCreateAmendCgtPpdOverridesRequestParser
import v1.mocks.services.{MockCreateAmendCgtPpdOverridesService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.createAmendCgtPpdOverrides._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendCgtPpdOverridesControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockAppConfig
  with MockCreateAmendCgtPpdOverridesService
  with MockHateoasFactory
  with MockCreateAmendCgtPpdOverridesRequestParser
  with HateoasLinks
  with MockIdGenerator {

  val nino: String = "AA123456A"
  val taxYear: String = "2019-20"
  val correlationId: String = "X-123"

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

  val requestModel: CreateAmendCgtPpdOverridesRequestBody = CreateAmendCgtPpdOverridesRequestBody(
    multiplePropertyDisposals= Some(Seq(
      MultiplePropertyDisposals(
        "AB0000000092",
        Some(1234.78),
        None
      ),
      MultiplePropertyDisposals(
        "AB0000000098",
        None,
        Some(134.99)
      )
    )),
    singlePropertyDisposals = Some(Seq(
        SinglePropertyDisposals(
          "AB0000000098",
          "2020-02-28",
          454.24,
          "2020-03-29",
          3434.45,
          233.45,
          423.34,
          2324.67,
          3434.23,
          Some(436.23),
          Some(234.23),
          Some(4567.89),
          None
        ),
        SinglePropertyDisposals(
          "AB0000000091",
          "2020-02-28",
          454.24,
          "2020-03-29",
          3434.45,
          233.45,
          423.34,
          2324.67,
          3434.23,
          Some(436.23),
          Some(234.23),
          None,
          Some(4567.89)
        )
      )
    )
  )

  val requestData: CreateAmendCgtPpdOverridesRequest = CreateAmendCgtPpdOverridesRequest(
    nino = Nino(nino),
    taxYear = taxYear,
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

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateAmendCgtPpdOverridesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockCreateAmendCgtPpdOverridesRequestParser,
      service = mockCreateAmendCgtPpdOverridesService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.apiGatewayContext.returns("individuals/income-received").anyNumberOfTimes()
    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "CreateAmendCgtPpdOverridesController" should {
    "return OK" when {
      "happy path" in new Test {

        MockCreateAmendCgtPpdOverridesRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockCreateAmendCgtPpdOverridesService
          .createAmend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, Unit))))

        val result: Future[Result] = controller.createAmendCgtPpdOverrides(nino, taxYear)(fakePutRequest(validRequestJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateAmendCgtPpdOverridesRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.createAmendCgtPpdOverrides(nino, taxYear)(fakePutRequest(validRequestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (RuleAmountGainLossError, BAD_REQUEST),
          (DateFormatError, BAD_REQUEST),
          (PPDSubmissionIdFormatError, BAD_REQUEST),
          (RuleLossesGreaterThanGainError, BAD_REQUEST),
          (RuleTaxYearNotEndedError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateAmendCgtPpdOverridesRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockCreateAmendCgtPpdOverridesService
              .createAmend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.createAmendCgtPpdOverrides(nino, taxYear)(fakePutRequest(validRequestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotEndedError, BAD_REQUEST),
          (PPDSubmissionIdNotFoundError, NOT_FOUND),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
