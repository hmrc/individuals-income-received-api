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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import api.connectors.DownstreamUri.{DesUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import v1.models.request.deleteCgtPpdOverrides.DeleteCgtPpdOverridesRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteCgtPpdOverridesConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def deleteCgtPpdOverrides(request: DeleteCgtPpdOverridesRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String) : Future[DownstreamOutcome[Unit]] ={

    import request._

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[Unit](s"income-tax/income/disposals/residential-property/ppd/${taxYear.asTysDownstream}/${nino.nino}")
    } else {
      DesUri[Unit](s"income-tax/income/disposals/residential-property/ppd/${nino.nino}/${taxYear.asMtd}")
    }

    delete(downstreamUri)
  }

}
