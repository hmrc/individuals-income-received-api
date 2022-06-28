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
import api.models.outcomes.{ResponseWrapper, DesResponse}
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.RetrieveUkSavingsAccountAnnualSummaryConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUkSavingsAccountAnnualSummaryService @Inject()(connector: RetrieveUkSavingsAccountAnnualSummaryConnector)
  extends DownstreamResponseMappingSupport with Logging {

  def retrieveUkSavingsAccountAnnualSummary(request: RetrieveUkSavingsAccountAnnualSummaryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveUkSavingsAccountAnnualSummaryResponse]]] = {
    desResponse =>
        desResponse.responseData match {
          case RetrieveUkSavingsAccountAnnualSummaryResponse(x) if x.length == 1 => Right(DesResponse(desResponse.correlationId, x.head.toMtd))
          case RetrieveUkSavingsAccountAnnualSummaryResponse(x) if x.isEmpty => Left(ErrorWrapper(desResponse.correlationId, NotFoundError, None))
          case _ =>
            logger.info(
              s"[RetrieveUkSavingsAccountAnnualSummaryService] [retrieve] [CorrelationId - ${desResponse.correlationId}] - " +
                "More than one matching account found")
            Left(ErrorWrapper(desResponse.correlationId, DownstreamError, None))
        }

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveUkSavingsAccountAnnualSummary(request)).leftMap(mapDesErrors(mappingDesToMtdError))
    } yield desResponseWrapper

    result.value
  }

  private def mappingDesToMtdError: Map[String, MtdError] = Map(
    "INVALID_NINO"              -> NinoFormatError,
    "INVALID_TYPE"              -> StandardDownstreamError,
    "INVALID_TAXYEAR"           -> TaxYearFormatError,
    "INVALID_INCOME_SOURCE"     -> SavingsAccountIdFormatError,
    "NOT_FOUND_PERIOD"          -> NotFoundError,
    "NOT_FOUND_INCOME_SOURCE"   -> NotFoundError,
    "SERVER_ERROR"              -> StandardDownstreamError,
    "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
  )

}
