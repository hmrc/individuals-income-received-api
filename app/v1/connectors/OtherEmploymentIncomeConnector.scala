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

import api.connectors.DownstreamUri.{DesUri, TaxYearSpecificIfsUri}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.otherEmploymentIncome.OtherEmploymentIncomeRequest
import v1.models.response.retrieveOtherEmployment.RetrieveOtherEmploymentResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherEmploymentIncomeConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def deleteOtherEmploymentIncome(request: OtherEmploymentIncomeRequest)(implicit
                                                                         hc: HeaderCarrier,
                                                                         ec: ExecutionContext,
                                                                         correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    implicit val successCode: SuccessCode = SuccessCode(NO_CONTENT)

    val downstreamUri =
      if (request.taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[Unit](s"income-tax/income/other/employments/${request.taxYear.asTysDownstream}/${request.nino}")
      } else {
        DesUri[Unit](s"income-tax/income/other/employments/${request.nino}/${request.taxYear.asMtd}")
      }

    val intent = hc.otherHeaders.toMap.get("Accept") match {
      case Some("application/vnd.hmrc.1.0+json") => Some("IIR")
      case _ => None
    }

    delete(
      uri = downstreamUri,
      intent = intent
    )
  }

  def retrieveOtherEmploymentIncome(request: OtherEmploymentIncomeRequest)(implicit
                                                                           hc: HeaderCarrier,
                                                                           ec: ExecutionContext,
                                                                           correlationId: String): Future[DownstreamOutcome[RetrieveOtherEmploymentResponse]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._

    val resolvedDownstreamUri = if (request.taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveOtherEmploymentResponse](
        s"income-tax/income/other/employments/${request.taxYear.asTysDownstream}/${request.nino}")
    } else {
      DesUri[RetrieveOtherEmploymentResponse](
        s"income-tax/income/other/employments/${request.nino}/${request.taxYear.asMtd}"
      )
    }

    val intent = hc.otherHeaders.toMap.get("Accept") match {
      case Some("application/vnd.hmrc.1.0+json") => Some("IIR")
      case _ => None
    }

    get(uri = resolvedDownstreamUri, intent = intent)
  }

}
