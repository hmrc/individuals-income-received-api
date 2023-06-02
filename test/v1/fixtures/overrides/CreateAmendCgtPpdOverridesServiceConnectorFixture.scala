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

package v1.fixtures.overrides

import v1.models.request.createAmendCgtPpdOverrides.{CreateAmendCgtPpdOverridesRequestBody, MultiplePropertyDisposals, SinglePropertyDisposals}

object CreateAmendCgtPpdOverridesServiceConnectorFixture {

  val multiplePropertyDisposalsModels: Seq[MultiplePropertyDisposals] =
    Seq(
      MultiplePropertyDisposals(
        "AB0000000092",
        Some(1234.78),
        None
      ),
      MultiplePropertyDisposals(
        "AB0000000098",
        None,
        Some(134.99)
      )
    )

  val singlePropertyDisposalsModels: Seq[SinglePropertyDisposals] =
    Seq(
      SinglePropertyDisposals(
        "AB0000000098",
        "2020-02-28",
        454.24,
        Some("2020-03-29"),
        3434.45,
        233.45,
        423.34,
        2324.67,
        3434.23,
        Some(436.23),
        Some(234.23),
        Some(4567.89),
        None
      ),
      SinglePropertyDisposals(
        "AB0000000091",
        "2020-02-28",
        454.24,
        Some("2020-03-29"),
        3434.45,
        233.45,
        423.34,
        2324.67,
        3434.23,
        Some(436.23),
        Some(234.23),
        None,
        Some(4567.89)
      )
    )

  val requestBodyModel: CreateAmendCgtPpdOverridesRequestBody =
    CreateAmendCgtPpdOverridesRequestBody(
      Some(multiplePropertyDisposalsModels),
      Some(singlePropertyDisposalsModels)
    )

}
