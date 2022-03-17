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

import api.controllers.EndpointLogContext
import api.models.errors.{EmploymentIdFormatError, ErrorWrapper, MtdError, NinoFormatError, NotFoundError, RuleCustomEmploymentUnignoreError, RuleTaxYearNotEndedError, StandardDownstreamError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import cats.implicits._

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1r7.connectors.UnignoreEmploymentConnector
import v1r7.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UnignoreEmploymentService @Inject()(connector: UnignoreEmploymentConnector) extends DownstreamResponseMappingSupport with Logging {

  def unignoreEmployment(request: IgnoreEmploymentRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.unignoreEmployment(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR" -> TaxYearFormatError,
      "INVALID_EMPLOYMENT_ID" -> EmploymentIdFormatError,
      "INVALID_CORRELATIONID" -> StandardDownstreamError,
      "CUSTOMER_ADDED" -> RuleCustomEmploymentUnignoreError,
      "NO_DATA_FOUND" -> NotFoundError,
      "BEFORE_TAX_YEAR_ENDED" -> RuleTaxYearNotEndedError,
      "SERVER_ERROR" -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE" -> StandardDownstreamError
    )
}