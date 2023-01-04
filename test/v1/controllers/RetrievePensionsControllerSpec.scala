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

import api.controllers.ControllerBaseSpec
import api.hateoas.HateoasLinks
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{AMEND_PENSIONS_INCOME, DELETE_PENSIONS_INCOME, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrievePensionsControllerFixture
import v1.mocks.requestParsers.MockRetrievePensionsRequestParser
import v1.mocks.services.MockRetrievePensionsService
import v1.models.request.retirevePensions.{RetrievePensionsRawData, RetrievePensionsRequest}
import v1.models.response.retrievePensions.{ForeignPensionsItem, OverseasPensionContributions, RetrievePensionsHateoasData, RetrievePensionsResponse}

import scala.concurrent.Future

class RetrievePensionsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrievePensionsService
    with MockHateoasFactory
    with MockRetrievePensionsRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val nino: String          = "AA123456A"
  val taxYear: String       = "2019-20"
  val correlationId: String = "X-123"

  val rawData: RetrievePensionsRawData = RetrievePensionsRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: RetrievePensionsRequest = RetrievePensionsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  val amendPensionsLink: Link =
    Link(
      href = s"/individuals/income-received/pensions/$nino/$taxYear",
      method = PUT,
      rel = AMEND_PENSIONS_INCOME
    )

  val retrievePensionsLink: Link =
    Link(
      href = s"/individuals/income-received/pensions/$nino/$taxYear",
      method = GET,
      rel = SELF
    )

  val deletePensionsLink: Link =
    Link(
      href = s"/individuals/income-received/pensions/$nino/$taxYear",
      method = DELETE,
      rel = DELETE_PENSIONS_INCOME
    )

  private val foreignPensionsItemModel = Seq(
    ForeignPensionsItem(
      countryCode = "DEU",
      amountBeforeTax = Some(100.23),
      taxTakenOff = Some(1.23),
      specialWithholdingTax = Some(2.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 3.23
    ),
    ForeignPensionsItem(
      countryCode = "FRA",
      amountBeforeTax = Some(200.25),
      taxTakenOff = Some(1.27),
      specialWithholdingTax = Some(2.50),
      foreignTaxCreditRelief = true,
      taxableAmount = 3.50
    )
  )

  private val overseasPensionContributionsItemModel = Seq(
    OverseasPensionContributions(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 200.23,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(4.23),
      dblTaxationCountryCode = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-123456")
    ),
    OverseasPensionContributions(
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

  private val retrievePensionsResponseModel = RetrievePensionsResponse(
    submittedOn = "2020-07-06T09:37:17Z",
    foreignPensions = Some(foreignPensionsItemModel),
    overseasPensionContributions = Some(overseasPensionContributionsItemModel)
  )

  private val mtdResponse = RetrievePensionsControllerFixture.mtdResponseWithHateoas(nino, taxYear)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrievePensionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrievePensionsRequestParser,
      service = mockRetrievePensionsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "RetrievePensionsController" should {
    "return OK" when {
      "the request is valid" in new Test {

        MockRetrievePensionsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrievePensionsService
          .retrievePensions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrievePensionsResponseModel))))

        MockHateoasFactory
          .wrap(retrievePensionsResponseModel, RetrievePensionsHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              retrievePensionsResponseModel,
              Seq(
                amendPensionsLink,
                retrievePensionsLink,
                deletePensionsLink
              )))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrievePensionsRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)

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
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrievePensionsRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrievePensionsService
              .retrievePensions(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
