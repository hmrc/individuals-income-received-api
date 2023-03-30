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

package v1.services

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import play.api.libs.json.{Format, Json}
import v1.mocks.connectors.MockRetrievePensionsConnector
import v1.models.request.retrievePensions.RetrievePensionsRequest
import v1.models.response.retrievePensions.RetrievePensionsResponse

import scala.concurrent.Future

class RetrievePensionsServiceSpec extends ServiceSpec {

  private val nino    = Nino("AA112233A")
  private val taxYear = "2019-20"

  val responseBody: RetrievePensionsResponse = RetrievePensionsResponse(
    submittedOn = "2020-07-06T09:37:17Z",
    foreignPensions = None,
    overseasPensionContributions = None
  )

  private val requestData = RetrievePensionsRequest(nino, TaxYear.fromMtd(taxYear))

  trait Test extends MockRetrievePensionsConnector {

    case class Data(field: Option[String])

    object Data {
      implicit val reads: Format[Data] = Json.format[Data]
    }

    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: RetrievePensionsService = new RetrievePensionsService(
      connector = mockRetrievePensionsConnector
    )

  }

  "service" should {
    "retrievePensions" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, responseBody))

        MockRetrievePensionsConnector
          .retrievePensions(requestData)
          .returns(Future.successful(outcome))

        await(service.retrievePensions(requestData)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrievePensionsConnector
              .retrievePensions(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrievePensions(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError),
          ("RULE_INCORRECT_GOV_TEST_SCENARIO", RuleIncorrectGovTestScenarioError)
        )

        val extraTysErrors = List(
          ("INVALID_CORRELATION_ID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }

  }

}
