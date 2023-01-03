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

package v1.services

import api.controllers.EndpointLogContext
import api.models.errors.{
  ErrorWrapper,
  MtdError,
  NinoFormatError,
  RuleCessationDateBeforeTaxYearStartError,
  RuleStartDateAfterTaxYearEndError,
  RuleTaxYearNotEndedError,
  StandardDownstreamError,
  TaxYearFormatError
}
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import cats.implicits._

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.AddCustomEmploymentConnector
import v1.models.request.addCustomEmployment.AddCustomEmploymentRequest
import v1.models.response.addCustomEmployment.AddCustomEmploymentResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddCustomEmploymentService @Inject() (connector: AddCustomEmploymentConnector) extends DownstreamResponseMappingSupport with Logging {

  def addEmployment(request: AddCustomEmploymentRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[AddCustomEmploymentResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.addEmployment(request)).leftMap(mapDownstreamErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "NOT_SUPPORTED_TAX_YEAR"    -> RuleTaxYearNotEndedError,
      "INVALID_DATE_RANGE"        -> RuleStartDateAfterTaxYearEndError,
      "INVALID_CESSATION_DATE"    -> RuleCessationDateBeforeTaxYearStartError,
      "INVALID_PAYLOAD"           -> StandardDownstreamError,
      "INVALID_CORRELATIONID"     -> StandardDownstreamError,
      "SERVER_ERROR"              -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"       -> StandardDownstreamError
    )

}
