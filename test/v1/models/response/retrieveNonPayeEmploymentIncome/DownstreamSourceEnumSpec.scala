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
import support.UnitSpec
import utils.enums.EnumJsonSpecSupport

class DownstreamSourceEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[DownstreamSourceEnum](
    ("HMRC HELD", DownstreamSourceEnum.`HMRC HELD`),
    ("CUSTOMER", DownstreamSourceEnum.CUSTOMER),
    ("LATEST", DownstreamSourceEnum.LATEST)
  )

  "toMtdEnum" must {
    "return the expected 'MtdSourceEnum' object" in {
      DownstreamSourceEnum.`HMRC HELD`.toMtdEnum shouldBe MtdSourceEnum.hmrcHeld
      DownstreamSourceEnum.CUSTOMER.toMtdEnum shouldBe MtdSourceEnum.user
      DownstreamSourceEnum.LATEST.toMtdEnum shouldBe MtdSourceEnum.latest
    }
  }

}
