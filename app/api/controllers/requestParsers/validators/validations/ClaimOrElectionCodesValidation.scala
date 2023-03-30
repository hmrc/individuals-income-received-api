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

package api.controllers.requestParsers.validators.validations

import api.models.domain.ClaimOrElectionCodes
import api.models.errors.{ClaimOrElectionCodesFormatError, MtdError}

import scala.util.{Failure, Success, Try}

object ClaimOrElectionCodesValidation {

  def validateOptional(claimOrElectionCodesO: Option[Seq[String]], index: Int): List[MtdError] = claimOrElectionCodesO match {
    case None => NoValidationErrors
    case Some(claimOrElectionCodes) if claimOrElectionCodes.isEmpty =>
      List(ClaimOrElectionCodesFormatError.copy(paths = Some(Seq(s"/disposals/$index/claimOrElectionCodes"))))
    case Some(claimOrElectionCodes) => validate(claimOrElectionCodes, index)
  }

  private def validate(claimOrElectionCodes: Seq[String], disposalsIndex: Int): List[MtdError] = {
    claimOrElectionCodes.zipWithIndex.flatMap { case (claimOrElectionCode, claimOrElectionCodesIndex) =>
      Try {
        Option(claimOrElectionCode).map(ClaimOrElectionCodes.parser)
      } match {
        case Failure(_) => List(s"/disposals/$disposalsIndex/claimOrElectionCodes/$claimOrElectionCodesIndex")
        case Success(_) => List()
      }
    }.toList match {
      case Nil  => NoValidationErrors
      case list => List(ClaimOrElectionCodesFormatError.copy(paths = Some(list)))
    }
  }

}
