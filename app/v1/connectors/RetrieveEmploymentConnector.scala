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

import api.connectors.DownstreamUri.Release6Uri
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveEmployment.RetrieveEmploymentRequest
import v1.models.response.retrieveEmployment.RetrieveEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveEmploymentConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveEmploymentRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveEmploymentResponse]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._
    import request._

    val downstreamUri: Release6Uri[RetrieveEmploymentResponse] = Release6Uri[RetrieveEmploymentResponse](
      s"income-tax/income/employments/$nino/$taxYear?employmentId=$employmentId"
    )

    get(uri = downstreamUri)
  }

}
