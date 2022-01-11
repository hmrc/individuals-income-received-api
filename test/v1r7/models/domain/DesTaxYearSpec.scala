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

package v1r7.models.domain

import support.UnitSpec

class DesTaxYearSpec extends UnitSpec {
  private val desYear = "2019"
  private val mtdYear = "2018-19"

  "fromMtd" should {
    "return a des tax year" in {
      DesTaxYear.fromMtd(mtdYear) shouldBe DesTaxYear(desYear)
    }
  }

  "toMtd" should {
    "return an mtd tax year" in {
      DesTaxYear.toMtd(DesTaxYear(desYear)) shouldBe mtdYear
    }
  }
}
