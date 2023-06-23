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
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{AMEND_DIVIDENDS_INCOME, DELETE_DIVIDENDS_INCOME, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v2.fixtures.RetrieveDividendsFixtures
import v2.fixtures.RetrieveDividendsFixtures.responseModel
import v2.mocks.requestParsers.MockRetrieveDividendsRequestParser
import v2.mocks.services.MockRetrieveDividendsService
import v2.models.request.retrieveDividends.{RetrieveDividendsRawData, RetrieveDividendsRequest}
import v2.models.response.retrieveDividends._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveDividendsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveDividendsService
    with MockHateoasFactory
    with MockRetrieveDividendsRequestParser
    with MockIdGenerator {

  private val taxYear: String = "2019-20"

  private val rawData: RetrieveDividendsRawData = RetrieveDividendsRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: RetrieveDividendsRequest = RetrieveDividendsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val amendDividendsLink: Link =
    Link(
      href = s"/individuals/income-received/dividends/$nino/$taxYear",
      method = PUT,
      rel = AMEND_DIVIDENDS_INCOME
    )

  private val retrieveDividendsLink: Link =
    Link(
      href = s"/individuals/income-received/dividends/$nino/$taxYear",
      method = GET,
      rel = SELF
    )

  private val deleteDividendsLink: Link =
    Link(
      href = s"/individuals/income-received/dividends/$nino/$taxYear",
      method = DELETE,
      rel = DELETE_DIVIDENDS_INCOME
    )

  private val mtdResponse = RetrieveDividendsFixtures.mtdResponseWithHateoas(nino, taxYear)

  "RetrieveDividendsController" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        MockRetrieveDividendsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveDividendsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveDividendsHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              responseModel,
              List(
                amendDividendsLink,
                retrieveDividendsLink,
                deleteDividendsLink
              )))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "parser validation fails" in new Test {
        MockRetrieveDividendsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveDividendsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveDividendsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveDividendsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveDividendsRequestParser,
      service = mockRetrieveDividendsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    override protected def callController(): Future[Result] = controller.retrieveDividends(nino, taxYear)(fakeGetRequest)

  }

}
