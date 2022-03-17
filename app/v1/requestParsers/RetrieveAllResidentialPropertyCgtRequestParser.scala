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

package v1.requestParsers

import api.models.domain.{ MtdSourceEnum, Nino }
import api.requestParsers.RequestParser
import v1.models.request.retrieveAllResidentialPropertyCgt.{ RetrieveAllResidentialPropertyCgtRawData, RetrieveAllResidentialPropertyCgtRequest }
import v1.requestParsers.validators.RetrieveAllResidentialPropertyCgtValidator

import javax.inject.{ Inject, Singleton }

@Singleton
class RetrieveAllResidentialPropertyCgtRequestParser @Inject()(val validator: RetrieveAllResidentialPropertyCgtValidator)
    extends RequestParser[RetrieveAllResidentialPropertyCgtRawData, RetrieveAllResidentialPropertyCgtRequest] {

  override protected def requestFor(data: RetrieveAllResidentialPropertyCgtRawData): RetrieveAllResidentialPropertyCgtRequest =
    RetrieveAllResidentialPropertyCgtRequest(
      Nino(data.nino),
      data.taxYear,
      data.source.flatMap(MtdSourceEnum.parser.lift).getOrElse(MtdSourceEnum.latest)
    )
}
