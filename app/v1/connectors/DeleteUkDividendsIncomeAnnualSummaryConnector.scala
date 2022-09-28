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

import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.connectors.DownstreamUri.{DesUri, TaxYearSpecificIfsUri}
import config.AppConfig

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.deleteUkDividendsIncomeAnnualSummary.DeleteUkDividendsIncomeAnnualSummaryRequest
import play.api.http.Status.NO_CONTENT

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteUkDividendsIncomeAnnualSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def delete(request: DeleteUkDividendsIncomeAnnualSummaryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._
    import request.nino.nino
    import request.taxYear

    implicit val successCode: SuccessCode = SuccessCode(NO_CONTENT)

    val downstreamUri =
      if (taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[Unit](s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/dividends/annual")
      } else {
        DesUri[Unit](s"income-tax/nino/$nino/income-source/dividends/annual/${taxYear.asDownstream}")
      }

    post(uri = downstreamUri, body = JsObject.empty)
  }

}
