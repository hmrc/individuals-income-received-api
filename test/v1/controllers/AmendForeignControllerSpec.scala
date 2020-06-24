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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockAmendForeignRequestParser
import v1.mocks.services.{MockAmendForeignService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.domain.DesTaxYear
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendForeign._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAppConfig
    with MockAmendForeignService
    with MockAmendForeignRequestParser {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AmendForeignController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      appConfig = mockAppConfig,
      requestParser = mockAmendForeignRequestParser,
      service = mockAmendForeignService,
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
      |   "foreignEarnings": {
      |      "customerReference": "FOREIGNINCME123A",
      |      "earningsNotTaxableUK": 1999.99
      |   },
      |   "unremittableForeignIncome": [
      |       {
      |          "countryCode": "FRA",
      |          "amountInForeignCurrency": 1999.99,
      |          "amountTaxPaid": 1999.99
      |       },
      |       {
      |          "countryCode": "IND",
      |          "amountInForeignCurrency": 2999.99,
      |          "amountTaxPaid": 2999.99
      |       }
      |    ]
      |}
    """.stripMargin
  )

  val rawData: AmendForeignRawData = AmendForeignRawData(
    nino = nino,
    taxYear = taxYear,
    body = AnyContentAsJson(requestBodyJson)
  )

  val foreignEarning: ForeignEarnings = ForeignEarnings(
    customerReference = Some("FOREIGNINCME123A"),
    earningsNotTaxableUK = Some(1999.99)
  )

  val unremittableForeignIncomeItems: Seq[UnremittableForeignIncomeItem] = Seq(
    UnremittableForeignIncomeItem(
      countryCode = "FRA",
      amountInForeignCurrency = Some(1999.99),
      amountTaxPaid = Some(1999.99)
    ),
    UnremittableForeignIncomeItem(
      countryCode = "IND",
      amountInForeignCurrency = Some(2999.99),
      amountTaxPaid = Some(2999.99)
    )
  )

  val amendForeignRequestBody: AmendForeignRequestBody = AmendForeignRequestBody(
    foreignEarnings = Some(foreignEarning),
    unremittableForeignIncome = Some(unremittableForeignIncomeItems)
  )

  val requestData: AmendForeignRequest = AmendForeignRequest(
    nino = Nino(nino),
    taxYear = DesTaxYear.fromMtd(taxYear),
    body = amendForeignRequestBody
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href":"/baseUrl/foreign/$nino/$taxYear",
      |         "rel":"amend-foreign-income",
      |         "method":"PUT"
      |      },
      |      {
      |         "href":"/baseUrl/foreign/$nino/$taxYear",
      |         "rel":"self",
      |         "method":"GET"
      |      },
      |      {
      |         "href":"/baseUrl/foreign/$nino/$taxYear",
      |         "rel":"delete-foreign-income",
      |         "method":"DELETE"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  "AmendForeignController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAmendForeignRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendForeignService
          .amendForeign(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendForeign(nino, taxYear)(fakePutRequest(requestBodyJson))

        status(result) shouldBe OK
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendForeignRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.amendForeign(nino, taxYear)(fakePutRequest(requestBodyJson))

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
          (CountryCodeFormatError, BAD_REQUEST),
          (CountryCodeRuleError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (CustomerRefFormatError, BAD_REQUEST),
          (MissingFieldError, BAD_REQUEST),
          (WrongFieldTypeError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendForeignRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendForeignService
              .amendForeign(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.amendForeign(nino, taxYear)(fakePutRequest(requestBodyJson))

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
