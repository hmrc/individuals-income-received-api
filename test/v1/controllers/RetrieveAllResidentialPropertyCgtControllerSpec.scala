/*
 * Copyright 2022 HM Revenue & Customs
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
import api.models.domain.{MtdSourceEnum, Nino}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link, RelType}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveAllResidentialPropertyCgtControllerFixture._
import v1.mocks.requestParsers.MockRetrieveAllResidentialPropertyCgtRequestParser
import v1.mocks.services.MockRetrieveAllResidentialPropertyCgtService
import v1.models.request.retrieveAllResidentialPropertyCgt.{RetrieveAllResidentialPropertyCgtRawData, RetrieveAllResidentialPropertyCgtRequest}
import v1.models.response.retrieveAllResidentialPropertyCgt.{RetrieveAllResidentialPropertyCgtHateoasData, RetrieveAllResidentialPropertyCgtResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveAllResidentialPropertyCgtControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveAllResidentialPropertyCgtService
    with MockHateoasFactory
    with MockRetrieveAllResidentialPropertyCgtRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val nino: String           = "AA123456A"
  val taxYear: String        = "2019-20"
  val source: Option[String] = Some("latest")
  val correlationId: String  = "X-123"

  val rawData: RetrieveAllResidentialPropertyCgtRawData = RetrieveAllResidentialPropertyCgtRawData(
    nino = nino,
    taxYear = taxYear,
    source = source
  )

  val requestData: RetrieveAllResidentialPropertyCgtRequest = RetrieveAllResidentialPropertyCgtRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    source = MtdSourceEnum.latest
  )

  val retrieveAllCgtLink: Link =
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
      method = GET,
      rel = RelType.SELF
    )

  val createAndAmendPpdCgtLink: Link =
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
      method = PUT,
      rel = RelType.CREATE_AND_AMEND_CGT_PPD_OVERRIDES
    )

  val createAndAmendNonPpdCgtLink: Link =
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
      method = PUT,
      rel = RelType.CREATE_AND_AMEND_NON_PPD_CGT_AND_DISPOSALS
    )

  val deletePpdCgtLink: Link =
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear/ppd",
      method = DELETE,
      rel = RelType.DELETE_CGT_PPD_OVERRIDES
    )

  val deleteNonPpdCgtLink: Link =
    Link(
      href = s"/individuals/income-received/disposals/residential-property/$nino/$taxYear",
      method = DELETE,
      rel = RelType.DELETE_NON_PPD_CGT_AND_DISPOSALS
    )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveAllResidentialPropertyCgtController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveAllResidentialPropertyCgtRequestParser,
      service = mockRetrieveAllResidentialPropertyCgtService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)

    def desErrorMap: Map[String, MtdError] =
      Map(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "INVALID_VIEW"              -> SourceFormatError,
        "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
        "NO_DATA_FOUND"             -> NotFoundError,
        "INVALID_CORRELATIONID"     -> StandardDownstreamError,
        "SERVER_ERROR"              -> StandardDownstreamError,
        "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
      )

  }

  "retrieveAll" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveAllResidentialPropertyCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveAllResidentialPropertyCgtService
          .retrieve[RetrieveAllResidentialPropertyCgtResponse](desErrorMap)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, model))))

        MockHateoasFactory
          .wrap(model, RetrieveAllResidentialPropertyCgtHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              model,
              Seq(
                createAndAmendPpdCgtLink,
                deletePpdCgtLink,
                createAndAmendNonPpdCgtLink,
                deleteNonPpdCgtLink,
                retrieveAllCgtLink
              )))

        val result: Future[Result] = controller.retrieveAll(nino, taxYear, source)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponseWithHateoas(nino, taxYear)
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveAllResidentialPropertyCgtRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveAll(nino, taxYear, source)(fakeGetRequest)

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
          (SourceFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveAllResidentialPropertyCgtRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveAllResidentialPropertyCgtService
              .retrieve[RetrieveAllResidentialPropertyCgtResponse](desErrorMap)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveAll(nino, taxYear, source)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (SourceFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (StandardDownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
