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
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.support.DownstreamResponseMappingSupport
import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.CreateAmendCgtPpdOverridesConnector
import v1.models.request.createAmendCgtPpdOverrides.CreateAmendCgtPpdOverridesRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendCgtPpdOverridesService @Inject() (connector: CreateAmendCgtPpdOverridesConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def createAmend(request: CreateAmendCgtPpdOverridesRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.createAmend(request)).leftMap(mapDownstreamErrors(errorMap))
    } yield downstreamResponseWrapper

    result.value
  }

  private def errorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID"       -> NinoFormatError,
      "INVALID_TAX_YEAR"                -> TaxYearFormatError,
      "INVALID_CORRELATIONID"           -> StandardDownstreamError,
      "INVALID_PAYLOAD"                 -> StandardDownstreamError,
      "PPD_SUBMISSIONID_NOT_FOUND"      -> PpdSubmissionIdNotFoundError,
      "DUPLICATE_SUBMISSION"            -> RuleDuplicatedPpdSubmissionIdError,
      "NO_PPD_SUBMISSIONS_FOUND"        -> NotFoundError,
      "INVALID_REQUEST_BEFORE_TAX_YEAR" -> RuleTaxYearNotEndedError,
      "INVALID_DISPOSAL_TYPE"           -> RuleIncorrectDisposalTypeError,
      "SERVER_ERROR"                    -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"             -> StandardDownstreamError
    )

    val extraTysErrorMap = Map(
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrorMap
  }

}
