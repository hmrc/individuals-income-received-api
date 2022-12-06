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

package v1.services

import api.controllers.EndpointLogContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.DeleteNonPayeEmploymentConnector
import v1.models.request.deleteNonPayeEmployment.DeleteNonPayeEmploymentRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteNonPayeEmploymentService @Inject()(connector: DeleteNonPayeEmploymentConnector)
  extends DownstreamResponseMappingSupport
    with Logging {

  def deleteNonPayeEmployment(request: DeleteNonPayeEmploymentRequest)(implicit
                                                                       hc: HeaderCarrier,
                                                                       ec: ExecutionContext,
                                                                       logContext: EndpointLogContext,
                                                                       correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = EitherT(connector.deleteNonPayeEmployment(request)).leftMap(mapDownstreamErrors(errorMap))

    result.value
  }

  val errorMap: Map[String, MtdError] = {
    val downstreamErrors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )

    val extraTysErrors: Map[String, MtdError] = Map(
      "INVALID_CORRELATION_ID"    -> StandardDownstreamError,
      "NOT_FOUND"                 -> NotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError
    )

    downstreamErrors ++ extraTysErrors

  }

}
