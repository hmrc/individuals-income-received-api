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

package v1andv2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.HateoasLinks
import api.mocks.hateoas.MockHateoasFactory
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{AMEND_OTHER_INCOME, DELETE_OTHER_INCOME, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v1andv2.fixtures.RetrieveOtherControllerFixture
import v1andv2.mocks.requestParsers.MockRetrieveOtherRequestParser
import v1andv2.mocks.services.MockRetrieveOtherService
import v1andv2.models.request.retrieveOther.{RetrieveOtherRawData, RetrieveOtherRequest}
import v1andv2.models.response.retrieveOther._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveOtherControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveOtherService
    with MockHateoasFactory
    with MockRetrieveOtherRequestParser
    with HateoasLinks {

  val taxYear: String = "2019-20"

  val rawData: RetrieveOtherRawData = RetrieveOtherRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: RetrieveOtherRequest = RetrieveOtherRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  val amendOtherLink: Link =
    Link(
      href = s"/individuals/income-received/other/$nino/$taxYear",
      method = PUT,
      rel = AMEND_OTHER_INCOME
    )

  val retrieveOtherLink: Link =
    Link(
      href = s"/individuals/income-received/other/$nino/$taxYear",
      method = GET,
      rel = SELF
    )

  val deleteOtherLink: Link =
    Link(
      href = s"/individuals/income-received/other/$nino/$taxYear",
      method = DELETE,
      rel = DELETE_OTHER_INCOME
    )

  private val businessReceiptsItemModel = Seq(
    BusinessReceiptsItem(
      grossAmount = 5000.99,
      taxYear = "2018-19"
    ),
    BusinessReceiptsItem(
      grossAmount = 6000.99,
      taxYear = "2019-20"
    )
  )

  private val allOtherIncomeReceivedWhilstAbroadItemModel = Seq(
    AllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "FRA",
      amountBeforeTax = Some(1999.99),
      taxTakenOff = Some(2.23),
      specialWithholdingTax = Some(3.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 4.23,
      residentialFinancialCostAmount = Some(2999.99),
      broughtFwdResidentialFinancialCostAmount = Some(1999.99)
    ),
    AllOtherIncomeReceivedWhilstAbroadItem(
      countryCode = "IND",
      amountBeforeTax = Some(2999.99),
      taxTakenOff = Some(3.23),
      specialWithholdingTax = Some(4.23),
      foreignTaxCreditRelief = true,
      taxableAmount = 5.23,
      residentialFinancialCostAmount = Some(3999.99),
      broughtFwdResidentialFinancialCostAmount = Some(2999.99)
    )
  )

  private val overseasIncomeAndGainsModel = OverseasIncomeAndGains(gainAmount = 3000.99)

  private val chargeableForeignBenefitsAndGiftsModel = ChargeableForeignBenefitsAndGifts(
    transactionBenefit = Some(1999.99),
    protectedForeignIncomeSourceBenefit = Some(2999.99),
    protectedForeignIncomeOnwardGift = Some(3999.99),
    benefitReceivedAsASettler = Some(4999.99),
    onwardGiftReceivedAsASettler = Some(5999.99)
  )

  private val omittedForeignIncomeModel = OmittedForeignIncome(amount = 4000.99)

  private val retrieveOtherResponseModel = RetrieveOtherResponse(
    submittedOn = "2019-04-04T01:01:01Z",
    Some(businessReceiptsItemModel),
    Some(allOtherIncomeReceivedWhilstAbroadItemModel),
    Some(overseasIncomeAndGainsModel),
    Some(chargeableForeignBenefitsAndGiftsModel),
    Some(omittedForeignIncomeModel)
  )

  private val mtdResponse = RetrieveOtherControllerFixture.mtdResponseWithHateoas(nino, taxYear)

  "RetrieveOtherController" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockRetrieveOtherRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveOtherService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveOtherResponseModel))))

        MockHateoasFactory
          .wrap(retrieveOtherResponseModel, RetrieveOtherHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              retrieveOtherResponseModel,
              Seq(
                amendOtherLink,
                retrieveOtherLink,
                deleteOtherLink
              )))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveOtherRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveOtherRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveOtherService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveOtherController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveOtherRequestParser,
      service = mockRetrieveOtherService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveOther(nino, taxYear)(fakeGetRequest)

  }

}
