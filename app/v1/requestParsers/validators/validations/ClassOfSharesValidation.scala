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

package v1.requestParsers.validators.validations

import api.models.errors.{ClassOfSharesAcquiredFormatError, ClassOfSharesAwardedFormatError, MtdError}
import api.models.errors.ClassOfSharesAcquiredFormatError

object ClassOfSharesValidation {
  private val regex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$"

  def validate(classOfShares: String, acquired: Boolean): List[MtdError] = {
    if (classOfShares.matches(regex)) { NoValidationErrors }
    else if (!classOfShares.matches(regex) && acquired) { List(ClassOfSharesAcquiredFormatError) }
    else { List(ClassOfSharesAwardedFormatError) }
  }

}