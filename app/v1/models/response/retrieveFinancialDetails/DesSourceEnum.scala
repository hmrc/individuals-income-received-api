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

package v1.models.response.retrieveFinancialDetails

import play.api.libs.json.Format
import utils.enums.Enums
import v1.models.domain.MtdSourceEnum

sealed trait DesSourceEnum {
  def toMtdEnum: MtdSourceEnum
}

object DesSourceEnum {
  val parser: PartialFunction[String, DesSourceEnum] = Enums.parser[DesSourceEnum]
  implicit val format: Format[DesSourceEnum] = Enums.format[DesSourceEnum]

  case object `HMRC HELD` extends DesSourceEnum {
    override def toMtdEnum: MtdSourceEnum = MtdSourceEnum.hmrcHeld
  }

  case object CUSTOMER extends DesSourceEnum {
    override def toMtdEnum: MtdSourceEnum = MtdSourceEnum.user
  }

  case object LATEST extends DesSourceEnum {
    override def toMtdEnum: MtdSourceEnum = MtdSourceEnum.latest
  }
}
