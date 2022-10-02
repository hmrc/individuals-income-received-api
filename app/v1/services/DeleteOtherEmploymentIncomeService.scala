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

import api.connectors.DownstreamUri.DesUri
import api.controllers.EndpointLogContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.DeleteOtherEmploymentIncomeConnector
import v1.models.request.deleteOtherEmploymentIncome.DeleteOtherEmploymentIncomeRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteOtherEmploymentIncomeService @Inject() (connector: DeleteOtherEmploymentIncomeConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def delete(request: DeleteOtherEmploymentIncomeRequest, desUri: DesUri[Unit])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.deleteOtherEmploymentIncome(request, desUri)).leftMap(mapDownstreamErrors(errorMap))
    } yield desResponseWrapper

    result.value
  }

  private def errorMap: Map[String, MtdError] = {
    val errorMap = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "NO_DATA_FOUND"             -> NotFoundError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )

    val extraTysErrors = Map(
      "INVALID_INCOMESOURCE_TYPE"  -> StandardDownstreamError,
      "TAX_YEAR_NOT_SUPPORTED"     -> RuleTaxYearNotSupportedError,
      "INCOME_SOURCE_NOT_FOUND"    -> NotFoundError,
      "INCOMPATIBLE_INCOME_SOURCE" -> StandardDownstreamError
    )
    errorMap ++ extraTysErrors
  }

}
