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

package v1andv2.models.request.createAmendOtherCgt

import play.api.libs.json.{Json, OFormat}

case class NonStandardGains(carriedInterestGain: Option[BigDecimal],
                            carriedInterestRttTaxPaid: Option[BigDecimal],
                            attributedGains: Option[BigDecimal],
                            attributedGainsRttTaxPaid: Option[BigDecimal],
                            otherGains: Option[BigDecimal],
                            otherGainsRttTaxPaid: Option[BigDecimal]) {

  def isThreeFieldsEmpty: Boolean = carriedInterestGain.isEmpty && attributedGains.isEmpty && otherGains.isEmpty
}

object NonStandardGains {
  val empty: NonStandardGains = NonStandardGains(None, None, None, None, None, None)

  implicit val format: OFormat[NonStandardGains] = Json.format[NonStandardGains]
}
