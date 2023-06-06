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

package api.connectors

import org.scalatest.Inside
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.http.Status._
import play.api.test.Injecting
import support.{IntegrationBaseSpec, WireMockMethods}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, UpstreamErrorResponse}
import v1.fixtures.nrs.NrsFixture

class NrsProxyConnectorISpec
    extends IntegrationBaseSpec
    with NrsFixture
    with ScalaFutures
    with Inside
    with IntegrationPatience
    with WireMockMethods
    with Injecting {

  val path                       = s"/mtd-api-nrs-proxy/$nino/$event"
  val auth                       = "Bearer xyx"
  implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(auth)))

  val connector: NrsProxyConnector = inject[NrsProxyConnector]

  "NrsProxyConnector" when {
    "submission is successful" should {
      "return successfully" in {
        when(POST, path, headers = Map("Authorization" -> auth))
          .withRequestBody(body)
          .thenReturn(OK)

        connector.submit(nino, event, body).futureValue shouldBe Right(())
      }
    }

    "submission fails with a server error status" should {
      "return an error" in {
        when(POST, path, headers = Map("Authorization" -> auth))
          .withRequestBody(body)
          .thenReturn(INTERNAL_SERVER_ERROR)

        inside(connector.submit(nino, event, body).futureValue) { case Left(err) =>
          err shouldBe an[UpstreamErrorResponse]
        }
      }
    }

    "submission fails with a client error status" should {
      "return an error" in {
        when(POST, path, headers = Map("Authorization" -> auth))
          .withRequestBody(body)
          .thenReturn(BAD_REQUEST)

        inside(connector.submit(nino, event, body).futureValue) { case Left(err) =>
          err shouldBe an[UpstreamErrorResponse]
        }
      }
    }
  }

}
