/*
 * Copyright 2022 HM Revenue & Customs
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

import api.controllers.ControllerBaseSpec
import api.hateoas.HateoasLinks
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.MockIdGenerator
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.errors.MtdError
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveUkSavingsAccountAnnualSummaryControllerFixture

import scala.concurrent.Future

class RetrieveUkSavingsAccountAnnualSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    //TODO: add new service
    with HateoasLinks
    with MockIdGenerator {

  val nino: String          = "AA123456A"
  val taxYear: String       = "2019-20"
  val savingsAccountId: String = "ABCDE0123456789"
  val correlationId: String = "X-123"

  //TODO: Retrieve model

  //TODO: Retrieve Request Model

 //TODO: Hateoas links

  //TODO: Response model

  private val mtdResponse = RetrieveUkSavingsAccountAnnualSummaryControllerFixture
    .mtdRetrieveResponseWithHateaos(nino, taxYear,savingsAccountId )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveUkSavingsAccountAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockDeleteRetrieveRequestParser,
      service = mockDeleteRetrieveService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "RetrieveUkSavingsAccountSummaryControllerSpec" should {
    "return OK" when {
      "happy path" in new Test {

      }
    }

    "return the error as per the spec" when {
      "parsers errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
         s"a(n) ${error.code} is returned from the parser" in new Test {
           ???
         }
        }
        val input= Seq()
      }
    }
  }

}
