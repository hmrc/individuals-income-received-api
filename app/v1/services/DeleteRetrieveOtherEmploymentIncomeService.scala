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
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.DeleteRetrieveOtherEmploymentIncomeConnector
import v1.models.request.deleteOtherEmploymentIncome.DeleteOtherEmploymentIncomeRequest
import v1.models.request.retrieveOtherEmploymentIncome.RetrieveOtherEmploymentIncomeRequest
import v1.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteRetrieveOtherEmploymentIncomeService @Inject() (connector: DeleteRetrieveOtherEmploymentIncomeConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def delete(request: DeleteOtherEmploymentIncomeRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.deleteOtherEmploymentIncome(request)).leftMap(mapDownstreamErrors(errorMap))
    } yield desResponseWrapper

    result.value
  }

  def retrieve(request: RetrieveOtherEmploymentIncomeRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveOtherEmploymentResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveOtherEmploymentIncome(request))
        .leftMap(mapDownstreamErrors(errorMap))
    } yield desResponseWrapper

    result.value
  }

  private def errorMap: Map[String, MtdError] = {
    val errorMap = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "NO_DATA_FOUND"             -> NotFoundError, // TODO this should be not found
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )

    val extraTysErrors = Map(
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )
    errorMap ++ extraTysErrors
  }

}
