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
import api.models.domain.{MtdSourceEnum, Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link, RelType}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v1.fixtures.RetrieveAllResidentialPropertyCgtControllerFixture._
import v1.mocks.requestParsers.MockRetrieveAllResidentialPropertyCgtRequestParser
import v1.mocks.services.MockRetrieveAllResidentialPropertyCgtService
import v1.models.request.retrieveAllResidentialPropertyCgt.{RetrieveAllResidentialPropertyCgtRawData, RetrieveAllResidentialPropertyCgtRequest}
import v1.models.response.retrieveAllResidentialPropertyCgt.RetrieveAllResidentialPropertyCgtHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveAllResidentialPropertyCgtControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveAllResidentialPropertyCgtService
    with MockHateoasFactory
    with MockRetrieveAllResidentialPropertyCgtRequestParser
    with HateoasLinks
    with MockIdGenerator {

  val taxYear: String        = "2019-20"
  val source: Option[String] = Some("latest")

  val rawData: RetrieveAllResidentialPropertyCgtRawData = RetrieveAllResidentialPropertyCgtRawData(
    nino = nino,
    taxYear = taxYear,
    source = source
  )

  val requestData: RetrieveAllResidentialPropertyCgtRequest = RetrieveAllResidentialPropertyCgtRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
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

  "retrieveAll" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        MockRetrieveAllResidentialPropertyCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveAllResidentialPropertyCgtService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        MockHateoasFactory
          .wrap(responseModel, RetrieveAllResidentialPropertyCgtHateoasData(nino, taxYear))
          .returns(
            HateoasWrapper(
              responseModel,
              List(
                createAndAmendPpdCgtLink,
                deletePpdCgtLink,
                createAndAmendNonPpdCgtLink,
                deleteNonPpdCgtLink,
                retrieveAllCgtLink
              )))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponseWithHateoas(nino, taxYear))
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveAllResidentialPropertyCgtRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveAllResidentialPropertyCgtRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveAllResidentialPropertyCgtService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveAllResidentialPropertyCgtController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveAllResidentialPropertyCgtRequestParser,
      service = mockRetrieveAllResidentialPropertyCgtService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveAll(nino, taxYear, source)(fakeGetRequest)

  }

}
