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

package v1r6.controllers.requestParsers.validators.validations

import v1r6.models.domain.MtdSourceEnum
import v1r6.models.errors.{MtdError, SourceFormatError}

import scala.util.{Failure, Success, Try}

object EmploymentSourceValidation {

  def validate(source: String): List[MtdError] = {
    Try {
      Option(source).map(MtdSourceEnum.parser)
    } match {
      case Failure(_) => List(SourceFormatError)
      case Success(_) => NoValidationErrors
    }
  }
}
