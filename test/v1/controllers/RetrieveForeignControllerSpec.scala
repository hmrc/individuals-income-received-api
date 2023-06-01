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
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v1.fixtures.foreign.RetrieveForeignFixture
import v1.mocks.requestParsers.MockRetrieveForeignRequestParser
import v1.mocks.services.MockRetrieveForeignService
import v1.models.request.retrieveForeign.{RetrieveForeignRawData, RetrieveForeignRequest}
import v1.models.response.retrieveForeign.{ForeignEarnings, RetrieveForeignHateoasData, RetrieveForeignResponse, UnremittableForeignIncome}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveForeignService
    with MockHateoasFactory
    with MockRetrieveForeignRequestParser {

  private val taxYear: String = "2019-20"

  private val rawData: RetrieveForeignRawData = RetrieveForeignRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: RetrieveForeignRequest = RetrieveForeignRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  override val testHateoasLinks: Seq[Link] = Seq(
    hateoas.Link(href = s"/individuals/income-received/foreign/$nino/$taxYear", method = GET, rel = "self"),
    hateoas.Link(href = s"/individuals/income-received/foreign/$nino/$taxYear", method = PUT, rel = "create-and-amend-foreign-income"),
    hateoas.Link(href = s"/individuals/income-received/foreign/$nino/$taxYear", method = DELETE, rel = "delete-foreign-income")
  )

  private val fullForeignEarningsModel: ForeignEarnings = ForeignEarnings(
    customerReference = Some("FOREIGNINCME123A"),
    earningsNotTaxableUK = 1999.99
  )

  private val fullUnremittableForeignIncomeModel1: UnremittableForeignIncome = UnremittableForeignIncome(
    countryCode = "FRA",
    amountInForeignCurrency = 1999.99,
    amountTaxPaid = Some(1999.99)
  )

  private val fullUnremittableForeignIncomeModel2: UnremittableForeignIncome = UnremittableForeignIncome(
    countryCode = "IND",
    amountInForeignCurrency = 2999.99,
    amountTaxPaid = Some(2999.99)
  )

  private val retrieveForeignResponse = RetrieveForeignResponse(
    submittedOn = "2019-04-04T01:01:01Z",
    foreignEarnings = Some(fullForeignEarningsModel),
    unremittableForeignIncome = Some(
      Seq(
        fullUnremittableForeignIncomeModel1,
        fullUnremittableForeignIncomeModel2
      ))
  )

  private val mtdResponse = RetrieveForeignFixture.mtdResponseWithHateoas(nino, taxYear)

  "RetrieveForeignController" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockRetrieveForeignRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveForeignService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveForeignResponse))))

        MockHateoasFactory
          .wrap(retrieveForeignResponse, RetrieveForeignHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveForeignResponse, testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveForeignRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveForeignRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveForeignService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveForeignController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveForeignRequestParser,
      service = mockRetrieveForeignService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveForeign(nino, taxYear)(fakeGetRequest)
  }

}
