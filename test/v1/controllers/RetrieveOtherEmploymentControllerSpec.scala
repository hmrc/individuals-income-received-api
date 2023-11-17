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
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{AMEND_OTHER_EMPLOYMENT_INCOME, DELETE_OTHER_EMPLOYMENT_INCOME, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v1.fixtures.OtherIncomeEmploymentFixture.retrieveOtherResponseModel
import v1.fixtures.RetrieveOtherEmploymentControllerFixture._
import v1.mocks.requestParsers.MockOtherEmploymentIncomeRequestParser
import v1.mocks.services.MockRetrieveOtherEmploymentIncomeService
import v1.models.request.otherEmploymentIncome.{OtherEmploymentIncomeRequest, OtherEmploymentIncomeRequestRawData}
import v1.models.response.retrieveOtherEmployment._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherEmploymentControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveOtherEmploymentIncomeService
    with MockHateoasFactory
    with MockOtherEmploymentIncomeRequestParser
    with HateoasLinks {

  val taxYear: String = "2019-20"

  val rawData: OtherEmploymentIncomeRequestRawData = OtherEmploymentIncomeRequestRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: OtherEmploymentIncomeRequest = OtherEmploymentIncomeRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val amendLink: Link = Link(
    href = s"/individuals/income-received/employments/other/$nino/$taxYear",
    method = PUT,
    rel = AMEND_OTHER_EMPLOYMENT_INCOME
  )

  private val deleteLink: Link = Link(
    href = s"/individuals/income-received/employments/other/$nino/$taxYear",
    method = DELETE,
    rel = DELETE_OTHER_EMPLOYMENT_INCOME
  )

  private val retrieveLink: Link = Link(
    href = s"/individuals/income-received/employments/other/$nino/$taxYear",
    method = GET,
    rel = SELF
  )

  "RetrieveOtherEmploymentIncomeController" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockOtherEmploymentIncomeRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveOtherEmploymentIncomeService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveOtherResponseModel))))

        MockHateoasFactory
          .wrap(retrieveOtherResponseModel, RetrieveOtherEmploymentHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              retrieveOtherResponseModel,
              Seq(
                amendLink,
                retrieveLink,
                deleteLink
              )))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseWithHateoas(nino, taxYear)))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockOtherEmploymentIncomeRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockOtherEmploymentIncomeRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveOtherEmploymentIncomeService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveOtherEmploymentController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockOtherEmploymentIncomeRequestParser,
      service = mockRetrieveOtherEmploymentIncomeService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveOther(nino, taxYear)(fakeGetRequest)

  }

}
