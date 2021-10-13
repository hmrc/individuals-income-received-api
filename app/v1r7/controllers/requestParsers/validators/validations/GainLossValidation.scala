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

package v1r7.controllers.requestParsers.validators.validations

import v1r7.models.errors.MtdError

object GainLossValidation {

  def validate(gain: Option[BigDecimal], loss: Option[BigDecimal], error: MtdError, path: String): List[MtdError] = (gain, loss) match {
    case (Some(_), Some(_)) => List(error.copy(paths = Some(Seq(path))))
    case _                  => NoValidationErrors
  }
}