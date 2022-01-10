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

package v1r6.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import v1r6.services.NrsProxyService

trait MockNrsProxyService extends MockFactory {

  val mockNrsProxyService: NrsProxyService = mock[NrsProxyService]

  object MockNrsProxyService {
    def submitAsync(nino: String, notableEvent: String, body: JsValue): CallHandler[Unit] = {
      (mockNrsProxyService.submitAsync(_: String, _: String, _: JsValue)(_: HeaderCarrier))
        .expects(nino, notableEvent, body, *)
    }
  }

}
