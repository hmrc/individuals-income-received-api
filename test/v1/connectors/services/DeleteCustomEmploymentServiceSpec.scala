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

package v1.connectors.services

import api.connectors.DownstreamUri.DesUri
import api.controllers.EndpointLogContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import play.api.libs.json.{Format, Json}
import v1.mocks.connectors.MockDeleteCustomEmploymentConnector
import v1.services.DeleteCustomEmploymentService

import scala.concurrent.Future

class DeleteCustomEmploymentServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = "2019-20"

  trait Test extends MockDeleteCustomEmploymentConnector {

    case class Data(field: Option[String])

    object Data {
      implicit val reads: Format[Data] = Json.format[Data]
    }

    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    implicit val deleteDesUri: DesUri[Unit]     = DesUri[Unit](s"income-tax/income/savings/$nino/$taxYear")
    implicit val retrieveDesUri: DesUri[Data]   = DesUri[Data](s"income-tax/income/savings/$nino/$taxYear")

    val service: DeleteCustomEmploymentService = new DeleteCustomEmploymentService(
      connector = mockDeleteCustomEmploymentConnector
    )

  }

  "DeleteCustomEmploymentServiceSpec" when {
    "delete" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockDeleteCustomEmploymentConnector
          .delete()
          .returns(Future.successful(outcome))

        await(service.delete()) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockDeleteCustomEmploymentConnector
              .delete()
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.delete()) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
