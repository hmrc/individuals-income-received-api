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

package v1r7.services

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1r7.connectors.ListEmploymentsConnector
import v1r7.controllers.EndpointLogContext
import v1r7.models.errors._
import v1r7.models.outcomes.ResponseWrapper
import v1r7.models.request.listEmployments.ListEmploymentsRequest
import v1r7.models.response.listEmployment.{Employment, ListEmploymentResponse}
import v1r7.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListEmploymentsService @Inject()(connector: ListEmploymentsConnector) extends DesResponseMappingSupport with Logging {

  def listEmployments(request: ListEmploymentsRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[ListEmploymentResponse[Employment]]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.listEmployments(request)).leftMap(mapDesErrors(mappingDesToMtdError))
    } yield desResponseWrapper.map(des => des)

    result.value
  }

  private def mappingDesToMtdError: Map[String, MtdError] = Map(
    "INVALID_TAXABLE_ENTITY_ID"  -> NinoFormatError,
    "INVALID_TAX_YEAR"           -> TaxYearFormatError,
    "INVALID_EMPLOYMENT_ID"      -> DownstreamError,
    "INVALID_CORRELATIONID"      -> DownstreamError,
    "NO_DATA_FOUND"              -> NotFoundError,
    "SERVER_ERROR"               -> DownstreamError,
    "SERVICE_UNAVAILABLE"        -> DownstreamError
  )
}
