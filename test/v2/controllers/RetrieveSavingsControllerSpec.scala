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
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v2.fixtures.RetrieveSavingsControllerFixture
import v2.mocks.requestParsers.MockRetrieveSavingsRequestParser
import v2.mocks.services.MockRetrieveSavingsService
import v2.models.request.retrieveSavings.{RetrieveSavingsRawData, RetrieveSavingsRequest}
import v2.models.response.retrieveSavings.{ForeignInterestItem, RetrieveSavingsHateoasData, RetrieveSavingsResponse, Securities}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveSavingsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveSavingsService
    with MockHateoasFactory
    with MockRetrieveSavingsRequestParser {

  private val taxYear = "2019-20"

  private val rawData: RetrieveSavingsRawData = RetrieveSavingsRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: RetrieveSavingsRequest = RetrieveSavingsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val hateoasLinks = List(
    Link(href = s"/individuals/income-received/savings/$nino/$taxYear", method = PUT, rel = "create-and-amend-savings-income"),
    Link(href = s"/individuals/income-received/savings/$nino/$taxYear", method = GET, rel = "self"),
    Link(href = s"/individuals/income-received/savings/$nino/$taxYear", method = DELETE, rel = "delete-savings-income")
  )

  private val fullSecuritiesItemsModel = Securities(
    taxTakenOff = Some(100.0),
    grossAmount = 1455.0,
    netAmount = Some(123.22)
  )

  private val fullForeignInterestsModel = ForeignInterestItem(
    amountBeforeTax = Some(1232.22),
    countryCode = "DEU",
    taxTakenOff = Some(22.22),
    specialWithholdingTax = Some(22.22),
    taxableAmount = 2321.22,
    foreignTaxCreditRelief = Some(true)
  )

  private val retrieveSavingsResponseModel = RetrieveSavingsResponse(
    submittedOn = Timestamp("2019-04-04T01:01:01.000Z"),
    securities = Some(fullSecuritiesItemsModel),
    foreignInterest = Some(Seq(fullForeignInterestsModel))
  )

  private val mtdResponse = RetrieveSavingsControllerFixture.mtdResponseWithHateoas(nino, taxYear)

  "RetrieveSavingsController" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        MockRetrieveSavingsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveSavingsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveSavingsResponseModel))))

        MockHateoasFactory
          .wrap(retrieveSavingsResponseModel, RetrieveSavingsHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveSavingsResponseModel, hateoasLinks))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveSavingsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)

      }

      "the service returns an error" in new Test {
        MockRetrieveSavingsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveSavingsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)

      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveSavingsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveSavingsRequestParser,
      service = mockRetrieveSavingsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveSaving(nino, taxYear)(fakeGetRequest)
  }

}
