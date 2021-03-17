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

package v1.connectors

import config.AppConfig
import play.api.Logger
import play.api.libs.json.Writes
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import v1.connectors.DownstreamUri.{DesUri, IfsUri}

import scala.concurrent.{ExecutionContext, Future}

trait BaseDesConnector {
  val http: HttpClient
  val appConfig: AppConfig

  val logger: Logger = Logger(this.getClass)

  private[connectors] def desHeaderCarrier(implicit hc: HeaderCarrier, correlationId: String): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(s"Bearer ${appConfig.desToken}")))
      .withExtraHeaders("Environment" -> appConfig.desEnv, "CorrelationId" -> correlationId)

  private[connectors] def ifsHeaderCarrier(implicit hc: HeaderCarrier, correlationId: String): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(s"Bearer ${appConfig.ifsToken}")))
      .withExtraHeaders("Environment" -> appConfig.ifsEnv, "CorrelationId" -> correlationId)

  def post[Body: Writes, Resp](body: Body, uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                                                     hc: HeaderCarrier,
                                                                     httpReads: HttpReads[DesOutcome[Resp]],
                                                                     correlationId: String): Future[DesOutcome[Resp]] = {

    def doPost(implicit hc: HeaderCarrier): Future[DesOutcome[Resp]] = {
      http.POST(getBackendUri(uri), body)
    }

    doPost(getBackendHeaders(uri, hc, correlationId))
  }

  def get[Resp](uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                          hc: HeaderCarrier,
                                          httpReads: HttpReads[DesOutcome[Resp]],
                                          correlationId: String): Future[DesOutcome[Resp]] = {

    def doGet(implicit hc: HeaderCarrier): Future[DesOutcome[Resp]] =
      http.GET(getBackendUri(uri))

    doGet(getBackendHeaders(uri, hc, correlationId))
  }

  def delete[Resp](uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                             hc: HeaderCarrier,
                                             httpReads: HttpReads[DesOutcome[Resp]],
                                             correlationId: String): Future[DesOutcome[Resp]] = {

    def doDelete(implicit hc: HeaderCarrier): Future[DesOutcome[Resp]] = {
      http.DELETE(getBackendUri(uri))
    }

    doDelete(getBackendHeaders(uri, hc, correlationId))
  }

  def put[Body: Writes, Resp](body: Body, uri: DownstreamUri[Resp])(implicit ec: ExecutionContext,
                                                                    hc: HeaderCarrier,
                                                                    httpReads: HttpReads[DesOutcome[Resp]],
                                                                    correlationId: String): Future[DesOutcome[Resp]] = {

    def doPut(implicit hc: HeaderCarrier): Future[DesOutcome[Resp]] = {
      http.PUT(getBackendUri(uri), body)
    }

    doPut(getBackendHeaders(uri, hc, correlationId))
  }

  private def getBackendUri[Resp](uri: DownstreamUri[Resp]): String = uri match {
    case DesUri(value) => s"${appConfig.desBaseUrl}/$value"
    case IfsUri(value) => s"${appConfig.ifsBaseUrl}/$value"
  }

  private def getBackendHeaders[Resp](uri: DownstreamUri[Resp], hc: HeaderCarrier, correlationId: String): HeaderCarrier = uri match {
    case DesUri(_) => desHeaderCarrier(hc, correlationId)
    case IfsUri(_) => ifsHeaderCarrier(hc, correlationId)
  }
}