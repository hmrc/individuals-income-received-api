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

package api.requestParsers

import api.models.domain.Nino
import api.models.request
import api.models.request.{DeleteRetrieveRawData, DeleteRetrieveRequest}
import api.requestParsers.validators.DeleteRetrieveValidator

import javax.inject.{Inject, Singleton}

@Singleton
class DeleteRetrieveRequestParser @Inject() (val validator: DeleteRetrieveValidator)
    extends RequestParser[DeleteRetrieveRawData, DeleteRetrieveRequest] {

  override protected def requestFor(data: DeleteRetrieveRawData): DeleteRetrieveRequest =
    request.DeleteRetrieveRequest(Nino(data.nino), data.taxYear)

}
