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
import api.models.hateoas.Method.{GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import v1.mocks.requestParsers.MockRetrieveUkDividendsAnnualIncomeSummaryRequestParser
import v1.mocks.services.MockRetrieveUkDividendsAnnualIncomeSummaryService
import v1.models.request.retrieveUkDividendsAnnualIncomeSummary.{
  RetrieveUkDividendsAnnualIncomeSummaryRawData,
  RetrieveUkDividendsAnnualIncomeSummaryRequest
}
import v1.models.response.retrieveUkDividendsAnnualIncomeSummary.{
  RetrieveUkDividendsAnnualIncomeSummaryHateoasData,
  RetrieveUkDividendsAnnualIncomeSummaryResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkDividendsAnnualIncomeSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveUkDividendsAnnualIncomeSummaryService
    with MockRetrieveUkDividendsAnnualIncomeSummaryRequestParser
    with MockHateoasFactory {

  private val taxYear = "2019-20"

  private val rawData: RetrieveUkDividendsAnnualIncomeSummaryRawData = RetrieveUkDividendsAnnualIncomeSummaryRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: RetrieveUkDividendsAnnualIncomeSummaryRequest = RetrieveUkDividendsAnnualIncomeSummaryRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val hateoasLinks = List(
    Link(href = s"/individuals/income-received/uk-dividends/$nino/$taxYear", method = PUT, rel = "create-and-amend-uk-dividends-income"),
    Link(href = s"/individuals/income-received/uk-dividends/$nino/$taxYear", method = GET, rel = "self")
  )

  private val retrieveUkDividendsAnnualIncomeSummaryResponseModel = RetrieveUkDividendsAnnualIncomeSummaryResponse(
    Some(100.99),
    Some(100.99)
  )

  private val mtdResponse = Json.parse("""
      |{
      |  "ukDividends":100.99,
      |  "otherUkDividends":100.99,
      |  "links":[
      |     {"href":"/individuals/income-received/uk-dividends/AA123456A/2019-20","method":"PUT","rel":"create-and-amend-uk-dividends-income"},
      |     {"href":"/individuals/income-received/uk-dividends/AA123456A/2019-20","method":"GET","rel":"self"}]}
      |""".stripMargin)

  "RetrieveDividendsController" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        MockRetrieveUkDividendsAnnualIncomeSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveUkDividendsIncomeAnnualSummaryService
          .retrieveUkDividends(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveUkDividendsAnnualIncomeSummaryResponseModel))))

        MockHateoasFactory
          .wrap(retrieveUkDividendsAnnualIncomeSummaryResponseModel, RetrieveUkDividendsAnnualIncomeSummaryHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveUkDividendsAnnualIncomeSummaryResponseModel, hateoasLinks))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveUkDividendsAnnualIncomeSummaryRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveUkDividendsAnnualIncomeSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveUkDividendsIncomeAnnualSummaryService
          .retrieveUkDividends(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveUkDividendsAnnualIncomeSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveUkDividendsAnnualIncomeSummaryRequestParser,
      service = mockRetrieveUkDividendsAnnualIncomeSummaryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveUkDividends(nino, taxYear)(fakeGetRequest)

  }

}
