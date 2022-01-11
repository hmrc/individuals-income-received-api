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

package v1r6.mocks

import org.joda.time.DateTime
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import utils.CurrentDateTime

import java.time.LocalDate

trait MockCurrentDateTime extends MockFactory {

  val mockCurrentDateTime: CurrentDateTime = mock[CurrentDateTime]

  object MockCurrentDateTime {
    def getDateTime: CallHandler[DateTime]   = (mockCurrentDateTime.getDateTime _).expects()
    def getLocalDate: CallHandler[LocalDate] = (mockCurrentDateTime.getLocalDate _).expects()
  }
}
