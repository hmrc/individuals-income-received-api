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

package api.connectors

import config.FeatureSwitches

sealed trait DownstreamUri[Resp] {
  val value: String
}

object DownstreamUri {
  case class DesUri[Resp](value: String)                extends DownstreamUri[Resp]
  case class IfsUri[Resp](value: String)                extends DownstreamUri[Resp]
  case class TaxYearSpecificIfsUri[Resp](value: String) extends DownstreamUri[Resp]
  case class Release6Uri[Resp](value: String)           extends DownstreamUri[Resp]
  case class Api1661Uri[Resp](value: String)            extends DownstreamUri[Resp]

  /**
   * Use this for endpoints that are known to have a tax year specific IFS API.
   */
  def ifsUri[Resp](value: String)(implicit featureSwitches: FeatureSwitches): DownstreamUri[Resp] = {
    if (featureSwitches.isTaxYearSpecificApiEnabled) {
      TaxYearSpecificIfsUri(value)
    } else {
      IfsUri(value)
    }
  }

  def apply[Resp](value: String, ifFeatureEnabled: => DownstreamUri[Resp], ifFeatureDisabled: => DownstreamUri[Resp])(implicit featureSwitches: FeatureSwitches): DownstreamUri[Resp] = {
    if (featureSwitches.isTaxYearSpecificApiEnabled) {
      ifFeatureEnabled
    } else {
      ifFeatureDisabled
    }
  }
}
