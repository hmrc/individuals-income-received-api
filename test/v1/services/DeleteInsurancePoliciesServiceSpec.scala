/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.domain.Nino
import v1.connectors.DesUri
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockDeleteInsurancePoliciesConnector
import v1.models.domain.DesTaxYear
import v1.models.errors.{DesErrorCode, DesErrors, DownstreamError, ErrorWrapper, MtdError, NinoFormatError, NotFoundError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.insurancePolicies.DeleteRequest

import scala.concurrent.Future

class DeleteInsurancePoliciesServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2019"
  private val correlationId = "X-corr"

  private val deleteRetrieveRequest = DeleteRequest(
    nino = Nino(nino),
    taxYear = DesTaxYear(taxYear)
  )

  trait Test extends MockDeleteInsurancePoliciesConnector {

    case class Data(field: String)

    object Data {
      implicit val reads: Format[Data] = Json.format[Data]
    }

    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    implicit val deleteDesUri: DesUri[Unit] = DesUri[Unit](s"some-placeholder/savings/$nino/$taxYear")

    val service: DeleteInsurancePoliciesService = new DeleteInsurancePoliciesService(
      connector = mockDeleteInsurancePoliciesConnector
    )
  }

  "DeleteInsurancePoliciesService" when {
    "delete" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockDeleteRetrieveConnector.delete(deleteRetrieveRequest)
          .returns(Future.successful(outcome))

        await(service.delete(deleteRetrieveRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockDeleteRetrieveConnector.delete(deleteRetrieveRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.delete(deleteRetrieveRequest)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
