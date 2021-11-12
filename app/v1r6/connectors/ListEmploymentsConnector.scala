/*
 * Copyright 2021 HM Revenue & Customs
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

package v1r6.connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1r6.connectors.DownstreamUri.Release6Uri
import v1r6.models.request.listEmployments.ListEmploymentsRequest
import v1r6.models.response.listEmployment.{Employment, ListEmploymentResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListEmploymentsConnector @Inject()(val http: HttpClient,
                                         val appConfig: AppConfig) extends BaseDownstreamConnector {

  def listEmployments(request: ListEmploymentsRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[DesOutcome[ListEmploymentResponse[Employment]]] = {

    import v1r6.connectors.httpparsers.StandardDesHttpParser._

    val nino = request.nino.nino
    val taxYear = request.taxYear

    get(
      Release6Uri[ListEmploymentResponse[Employment]](s"income-tax/income/employments/$nino/$taxYear")
    )
  }
}
