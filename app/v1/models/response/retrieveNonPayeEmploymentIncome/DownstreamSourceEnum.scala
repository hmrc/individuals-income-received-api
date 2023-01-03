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

package v1.models.response.retrieveNonPayeEmploymentIncome

import api.models.domain.MtdSourceEnum
import play.api.libs.json.Format
import utils.enums.Enums

sealed trait DownstreamSourceEnum {
  def toMtdEnum: MtdSourceEnum
}

object DownstreamSourceEnum {
  implicit val format: Format[DownstreamSourceEnum] = Enums.format[DownstreamSourceEnum]

  case object `HMRC HELD` extends DownstreamSourceEnum {
    override def toMtdEnum: MtdSourceEnum = MtdSourceEnum.hmrcHeld
  }

  case object CUSTOMER extends DownstreamSourceEnum {
    override def toMtdEnum: MtdSourceEnum = MtdSourceEnum.user
  }

  case object LATEST extends DownstreamSourceEnum {
    override def toMtdEnum: MtdSourceEnum = MtdSourceEnum.latest
  }

}
