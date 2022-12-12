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
import api.models.errors.{
  ErrorWrapper,
  MtdError,
  NinoFormatError,
  RuleAcquisitionDateAfterDisposalDateError,
  RuleCompletionDateError,
  RuleDisposalDateError,
  StandardDownstreamError,
  TaxYearFormatError
}
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT

import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateAmendCgtResidentialPropertyDisposalsConnector
import v1.models.request.createAmendCgtResidentialPropertyDisposals.CreateAmendCgtResidentialPropertyDisposalsRequest

import scala.concurrent.{ExecutionContext, Future}

class CreateAmendCgtResidentialPropertyDisposalsService @Inject() (connector: CreateAmendCgtResidentialPropertyDisposalsConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createAndAmend(request: CreateAmendCgtResidentialPropertyDisposalsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.createAndAmend(request)).leftMap(mapDownstreamErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "INVALID_PAYLOAD"           -> StandardDownstreamError,
      "INVALID_DISPOSAL_DATE"     -> RuleDisposalDateError,
      "INVALID_COMPLETION_DATE"   -> RuleCompletionDateError,
      "INVALID_ACQUISITION_DATE"  -> RuleAcquisitionDateAfterDisposalDateError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )

}
