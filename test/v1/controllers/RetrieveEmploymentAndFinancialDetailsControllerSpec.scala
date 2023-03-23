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
import api.hateoas.HateoasLinks
import api.mocks.hateoas.MockHateoasFactory
import api.models.domain.{MtdSourceEnum, Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{AMEND_EMPLOYMENT_FINANCIAL_DETAILS, DELETE_EMPLOYMENT_FINANCIAL_DETAILS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v1.fixtures.RetrieveFinancialDetailsControllerFixture._
import v1.mocks.requestParsers.MockRetrieveEmploymentAndFinancialDetailsRequestParser
import v1.mocks.services.MockRetrieveEmploymentAndFinancialDetailsService
import v1.models.request.retrieveFinancialDetails.{RetrieveEmploymentAndFinancialDetailsRequest, RetrieveFinancialDetailsRawData}
import v1.models.response.retrieveFinancialDetails._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveEmploymentAndFinancialDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveEmploymentAndFinancialDetailsService
    with MockHateoasFactory
    with MockRetrieveEmploymentAndFinancialDetailsRequestParser
    with HateoasLinks {

  val taxYear: String      = "2017-18"
  val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val source: String       = "latest"

  val rawData: RetrieveFinancialDetailsRawData = RetrieveFinancialDetailsRawData(
    nino = nino,
    taxYear = taxYear,
    employmentId = employmentId,
    source = Some(source)
  )

  val requestData: RetrieveEmploymentAndFinancialDetailsRequest = RetrieveEmploymentAndFinancialDetailsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = employmentId,
    source = MtdSourceEnum.latest
  )

  val amendLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
      method = PUT,
      rel = AMEND_EMPLOYMENT_FINANCIAL_DETAILS
    )

  val retrieveLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
      method = GET,
      rel = SELF
    )

  val deleteLink: Link =
    Link(
      href = s"/individuals/income-received/employments/$nino/$taxYear/$employmentId/financial-details",
      method = DELETE,
      rel = DELETE_EMPLOYMENT_FINANCIAL_DETAILS
    )

  private val mtdResponse = mtdResponseWithHateoas(nino, taxYear, employmentId)

  "RetrieveFinancialDetailsController" should {
    "return OK" when {
      "happy path" in new Test {
        MockRetrieveFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveEmploymentAndFinancialDetailsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, model))))

        MockHateoasFactory
          .wrap(model, RetrieveFinancialDetailsHateoasData(nino, taxYear, employmentId))
          .returns(
            HateoasWrapper(
              model,
              Seq(
                retrieveLink,
                amendLink,
                deleteLink
              )))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveFinancialDetailsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveEmploymentAndFinancialDetailsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveEmploymentAndFinancialDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveFinancialDetailsRequestParser,
      service = mockRetrieveEmploymentAndFinancialDetailsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieve(nino, taxYear, employmentId, Some(source))(fakeRequest)
  }

}
