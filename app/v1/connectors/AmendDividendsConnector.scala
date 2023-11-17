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

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.amendDividends.AmendDividendsRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendDividendsConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def amendDividends(
                      request: AmendDividendsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    val uri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Unit](s"income-tax/income/dividends/${taxYear.asTysDownstream}/$nino")
    } else {
      // MTD format used pre-TYS
      IfsUri[Unit](s"income-tax/income/dividends/$nino/${taxYear.asMtd}")
    }

    val intent = hc.otherHeaders.toMap.get("Accept") match {
      case Some("application/vnd.hmrc.1.0+json") => Some("IIR")
      case _ => None
    }

    put(uri, request.body, intent = intent)
  }

}
