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

package api.models.domain

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait ClaimOrElectionCodes

object ClaimOrElectionCodes {

  case object PRR extends ClaimOrElectionCodes

  case object LET extends ClaimOrElectionCodes

  case object GHO extends ClaimOrElectionCodes

  case object ROR extends ClaimOrElectionCodes

  case object PRO extends ClaimOrElectionCodes

  case object ESH extends ClaimOrElectionCodes

  case object NVC extends ClaimOrElectionCodes

  case object SIR extends ClaimOrElectionCodes

  case object OTH extends ClaimOrElectionCodes

  case object BAD extends ClaimOrElectionCodes

  case object INV extends ClaimOrElectionCodes

  implicit val format: Format[ClaimOrElectionCodes]         = Enums.format[ClaimOrElectionCodes]
  val parser: PartialFunction[String, ClaimOrElectionCodes] = Enums.parser[ClaimOrElectionCodes]
}
