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
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
//import api.models.hateoas.RelType.{AMEND_PENSIONS_INCOME, DELETE_PENSIONS_INCOME, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.Result
import v1.fixtures.RetrievePensionsControllerFixture
import v1.mocks.requestParsers.MockRetrievePensionsRequestParser
import v1.mocks.services.MockRetrievePensionsService
import v1.models.request.retrievePensions.{RetrievePensionsRawData, RetrievePensionsRequest}
import v1.models.response.retrievePensions.{ForeignPensionsItem, OverseasPensionContributions, RetrievePensionsHateoasData, RetrievePensionsResponse}

import scala.concurrent.Future

class RetrievePensionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrievePensionsService
    with MockHateoasFactory
    with MockRetrievePensionsRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val taxYear: String = "2019-20"

  val rawData: RetrievePensionsRawData = RetrievePensionsRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: RetrievePensionsRequest = RetrievePensionsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val hateoasLinks = Seq(
    Link(href = s"/individuals/income-received/pensions/$nino/$taxYear", rel = "create-and-amend-pensions-income", method = PUT),
    Link(href = s"/individuals/income-received/pensions/$nino/$taxYear", rel = "self", method = GET),
    Link(href = s"/individuals/income-received/pensions/$nino/$taxYear", rel = "delete-pensions-income", method = DELETE)
  )
//  val amendPensionsLink: Link =
//    Link(
//      href = s"/individuals/income-received/pensions/$nino/$taxYear",
//      method = PUT,
//      rel = AMEND_PENSIONS_INCOME
//    )
//
//  val retrievePensionsLink: Link =
//    Link(
//      href = s"/individuals/income-received/pensions/$nino/$taxYear",
//      method = GET,
//      rel = SELF
//    )
//
//  val deletePensionsLink: Link =
//    Link(
//      href = s"/individuals/income-received/pensions/$nino/$taxYear",
//      method = DELETE,
//      rel = DELETE_PENSIONS_INCOME
//    )

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

  "RetrievePensionsController" should {
    "return a successful response with status 200 (OK)" when {
      "the request is valid" in new Test {

        MockRetrievePensionsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrievePensionsService
          .retrievePensions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrievePensionsResponseModel))))

        MockHateoasFactory
          .wrap(retrievePensionsResponseModel, RetrievePensionsHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrievePensionsResponseModel, hateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        MockRetrievePensionsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "service errors occur" in new Test {

        MockRetrievePensionsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrievePensionsService
          .retrievePensions(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrievePensionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrievePensionsRequestParser,
      service = mockRetrievePensionsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)
  }

}
