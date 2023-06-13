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
import api.models.domain.{Nino, TaxYear, Timestamp}
import api.models.errors._
import api.models.hateoas
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v1.fixtures.RetrieveInsurancePoliciesControllerFixture
import v1.mocks.requestParsers.MockRetrieveInsurancePoliciesRequestParser
import v1.mocks.services.MockRetrieveInsurancePoliciesService
import v1.models.request.retrieveInsurancePolicies.{RetrieveInsurancePoliciesRawData, RetrieveInsurancePoliciesRequest}
import v1.models.response.retrieveInsurancePolicies._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveInsurancePoliciesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveInsurancePoliciesService
    with MockHateoasFactory
    with MockRetrieveInsurancePoliciesRequestParser {

  val taxYear: String = "2019-20"

  private val rawData: RetrieveInsurancePoliciesRawData = RetrieveInsurancePoliciesRawData(
    nino = nino,
    taxYear = taxYear
  )

  private val requestData: RetrieveInsurancePoliciesRequest = RetrieveInsurancePoliciesRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  override val testHateoasLinks: Seq[Link] = Seq(
    hateoas.Link(
      href = s"/individuals/income-received/insurance-policies/$nino/$taxYear",
      method = PUT,
      rel = "create-and-amend-insurance-policies-income"),
    hateoas.Link(href = s"/individuals/income-received/insurance-policies/$nino/$taxYear", method = GET, rel = "self"),
    hateoas.Link(href = s"/individuals/income-received/insurance-policies/$nino/$taxYear", method = DELETE, rel = "delete-insurance-policies-income")
  )

  private val lifeInsuranceItemModel = CommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123A"),
    event = Some("Death of spouse"),
    gainAmount = 1.23,
    taxPaid = true,
    yearsHeld = Some(2),
    yearsHeldSinceLastGain = Some(1),
    deficiencyRelief = Some(1.23)
  )

  private val capitalRedemptionItemModel = CommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123B"),
    event = Some("Death of spouse"),
    gainAmount = 1.24,
    taxPaid = true,
    yearsHeld = Some(3),
    yearsHeldSinceLastGain = Some(2),
    deficiencyRelief = Some(1.23)
  )

  private val lifeAnnuityItemModel = CommonInsurancePoliciesItem(
    customerReference = Some("INPOLY123C"),
    event = Some("Death of spouse"),
    gainAmount = 1.25,
    taxPaid = true,
    yearsHeld = Some(4),
    yearsHeldSinceLastGain = Some(3),
    deficiencyRelief = Some(1.23)
  )

  private val voidedIsaItemModel = VoidedIsaPoliciesItem(
    customerReference = Some("INPOLY123D"),
    event = Some("Death of spouse"),
    gainAmount = 1.26,
    taxPaidAmount = Some(1.36),
    yearsHeld = Some(5),
    yearsHeldSinceLastGain = Some(4)
  )

  private val foreignItemModel = ForeignPoliciesItem(
    customerReference = Some("INPOLY123E"),
    gainAmount = 1.27,
    taxPaidAmount = Some(1.37),
    yearsHeld = Some(6)
  )

  private val retrieveInsurancePoliciesResponseModel = RetrieveInsurancePoliciesResponse(
    submittedOn = Timestamp("2020-07-06T09:37:17.000Z"),
    lifeInsurance = Some(Seq(lifeInsuranceItemModel)),
    capitalRedemption = Some(Seq(capitalRedemptionItemModel)),
    lifeAnnuity = Some(Seq(lifeAnnuityItemModel)),
    voidedIsa = Some(Seq(voidedIsaItemModel)),
    foreign = Some(Seq(foreignItemModel))
  )

  private val mtdResponse = RetrieveInsurancePoliciesControllerFixture.mtdResponseWithHateoas(nino, taxYear)

  "RetrieveInsurancePoliciesController" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockRetrieveInsurancePoliciesRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveInsurancePoliciesService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveInsurancePoliciesResponseModel))))

        MockHateoasFactory
          .wrap(retrieveInsurancePoliciesResponseModel, RetrieveInsurancePoliciesHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveInsurancePoliciesResponseModel, testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveInsurancePoliciesRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveInsurancePoliciesRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveInsurancePoliciesService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveInsurancePoliciesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveInsurancePoliciesRequestParser,
      service = mockRetrieveInsurancePoliciesService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveInsurancePolicies(nino, taxYear)(fakeGetRequest)
  }

}
