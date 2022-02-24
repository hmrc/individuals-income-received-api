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

package v1.connectors
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class CreateAmendCgtPpdOverridesConnector @Inject()(val http: HttpClient,
                                                    val appConfig: AppConfig) extends BaseDownstreamConnector {

  def createAmend(request: CreateAmendCgtPpdOverridesRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[DesOutcome[Unit]] = {

    val nino = request.nino.nino
    val taxYear = request.taxYear

    put(
      uri = Api1661Uri[Unit](s"income-tax/income/disposals/residential-property/ppd/$nino/$taxYear"), body = request.body
    )
  }
}
