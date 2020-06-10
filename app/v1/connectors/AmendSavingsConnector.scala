/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import v1.models.request.amendSavings.AmendSavingsRequest

import scala.concurrent.{ExecutionContext, Future}

class AmendSavingsConnector @Inject()(val http: HttpClient,
                                      val appConfig: AppConfig) extends BaseDesConnector {

  def amendSavings(request: AmendSavingsRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[DesOutcome[Unit]] = {

    import v1.connectors.httpparsers.StandardDesHttpParser._

    val nino = request.nino.nino
    val taxYear = request.taxYear.value

    put(
      uri = DesUri[Unit](s"some-placeholder/savings/$nino/$taxYear"), body = request.body
    )
  }
}
