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

import v1r7.models.errors.{MtdError, RuleAcquisitionDateError}

import java.time.LocalDate

object AcquisitionDateValidation {
  def validate(disposalDate: String, acquisitionDate: String, path: String): List[MtdError] = {
    if(LocalDate.parse(acquisitionDate).isAfter(LocalDate.parse(disposalDate))) {
      List(RuleAcquisitionDateError.copy(paths = Some(Seq(path))))
    } else {
      NoValidationErrors
    }
  }
}
