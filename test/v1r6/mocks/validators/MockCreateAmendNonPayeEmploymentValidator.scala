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

package v1r6.mocks.validators

import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import v1r6.controllers.requestParsers.validators.CreateAmendNonPayeEmploymentValidator
import v1r6.models.errors.MtdError
import v1r6.models.request.createAmendNonPayeEmployment.CreateAmendNonPayeEmploymentRawData

trait MockCreateAmendNonPayeEmploymentValidator extends MockFactory {

  val mockValidator: CreateAmendNonPayeEmploymentValidator =
    mock[CreateAmendNonPayeEmploymentValidator]

  object MockCreateAmendNonPayeEmploymentValidator {

    def validate(data: CreateAmendNonPayeEmploymentRawData): CallHandler1[CreateAmendNonPayeEmploymentRawData, List[MtdError]] = {
      (mockValidator
        .validate(_: CreateAmendNonPayeEmploymentRawData))
        .expects(data)
    }
  }

}