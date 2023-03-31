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
import v1.mocks.connectors.MockAmendDividendsConnector
import v1.models.request.amendDividends._

import scala.concurrent.Future

class AmendDividendsServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = TaxYear.fromMtd("2019-20")

  val amendDividendsRequest: AmendDividendsRequest = AmendDividendsRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = AmendDividendsRequestBody(None, None, None, None, None, None)
  )

  trait Test extends MockAmendDividendsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: AmendDividendsService = new AmendDividendsService(
      connector = mockAmendDividendsConnector
    )

  }

  "AmendDividendsService" when {
    "amendDividends" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendDividendsConnector
          .amendDividends(amendDividendsRequest)
          .returns(Future.successful(outcome))

        await(service.amendDividends(amendDividendsRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(errorCode: String, error: MtdError): Unit =
          s"a $errorCode error is returned from the service" in new Test {

            MockAmendDividendsConnector
              .amendDividends(amendDividendsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(errorCode))))))

            await(service.amendDividends(amendDividendsRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("INVALID_PAYLOAD", InternalError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
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
