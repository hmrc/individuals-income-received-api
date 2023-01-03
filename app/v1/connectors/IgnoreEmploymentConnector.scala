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

package v1.connectors

import api.connectors.BaseDownstreamConnector
import config.AppConfig

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import api.connectors.DownstreamUri.Release6Uri
import v1.models.request.ignoreEmployment.IgnoreEmploymentRequest

import scala.concurrent.{ExecutionContext, Future}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.request.EmptyBody

@Singleton
class IgnoreEmploymentConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def ignoreEmployment(
      request: IgnoreEmploymentRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    val nino         = request.nino.nino
    val taxYear      = request.taxYear
    val employmentId = request.employmentId

    put(EmptyBody, Release6Uri[Unit](s"income-tax/income/employments/$nino/$taxYear/$employmentId/ignore"))
  }

}
