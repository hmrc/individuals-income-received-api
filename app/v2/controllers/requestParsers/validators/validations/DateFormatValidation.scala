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

package v2.controllers.requestParsers.validators.validations

import api.controllers.requestParsers.validators.validations.{NoValidationErrors, dateFormat}
import api.models.errors.{DateFormatError, MtdError, RuleDateRangeInvalidError}

import java.time.LocalDate
import scala.util.Try

object DateFormatValidation {
  private def parseDate(date:String): Either[Throwable, LocalDate] = Try {LocalDate.parse(date, dateFormat)}.toEither
  private def isDateRangeValid(date:LocalDate): Boolean = date.getYear >= 1900 && date.getYear <= 2100
  private def convertPathToGivenError(error:MtdError, optionalPath: Option[String]): MtdError =
    optionalPath.map(p => error.copy(paths = Some(Seq(p)))).getOrElse(error)


  def validateWithSpecificFormatError(date: String, error: MtdError): List[MtdError] = parseDate(date) match {
    case Right(d) => if (isDateRangeValid(d)) NoValidationErrors else List(error)
    case Left(_) => List(error)
  }

  def validateOptional(date: Option[String], path: Option[String]=None): List[MtdError] = {

    def validate(date: String): List[MtdError] = parseDate(date) match {
      case Right(d) => if (isDateRangeValid(d)) NoValidationErrors else List(convertPathToGivenError(RuleDateRangeInvalidError, path))
      case _        => List(convertPathToGivenError(DateFormatError, path))
    }

    date match {
      case Some(date) => validate(date)
      case _          => NoValidationErrors
    }
  }

}
