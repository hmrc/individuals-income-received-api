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
import v1.connectors.CreateAmendUkSavingsAccountAnnualSummaryConnector
import v1.models.request.createAmendUkSavingsAnnualSummary.CreateAmendUkSavingsAnnualSummaryRequest

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateAmendUkSavingAccountAnnualSummaryService @Inject() (connector: CreateAmendUkSavingsAccountAnnualSummaryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createAmend(request: CreateAmendUkSavingsAnnualSummaryRequest)(implicit
                   hc: HeaderCarrier,
                   ec: ExecutionContext,
                   logContext: EndpointLogContext,
                    correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {
     val result = for {
          desResponseWrapper <- EitherT(connector.createOrAmendUKSavingsAccountSummary(request))
                    .leftMap(mapDesErrors(desErrorMap))
     } yield desResponseWrapper
     result.value
   }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_NINO"                    -> NinoFormatError,
      "INVALID_TAXYEAR"                 -> TaxYearFormatError,
      "INVALID_INCOME_SOURCE"           -> SavingsAccountIdFormatError,
      "INVALID_TYPE"                    -> StandardDownstreamError,
      "NOT_FOUND_PERIOD"                -> NotFoundError,
      "NOT_FOUND_INCOME_SOURCE"         -> NotFoundError,
      "SERVER_ERROR"                    -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"             -> StandardDownstreamError
    )
}
