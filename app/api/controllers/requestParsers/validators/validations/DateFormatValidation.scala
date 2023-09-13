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

import api.models.errors.{DateFormatError, MtdError}

import java.time.LocalDate
import scala.util.Try

object DateFormatValidation {

  def parseDate(date:String): Either[Throwable, LocalDate] = Try {LocalDate.parse(date, dateFormat)}.toEither

  def isDateRangeValid(date:LocalDate): Boolean = date.getYear >= 1900 && date.getYear <= 2100

  def convertPathToError(path:String): MtdError = DateFormatError.copy(paths = Some(Seq(path)))

  def validate(date: String, error: MtdError): List[MtdError] =
    parseDate(date) match {
      case Right(d) => if(isDateRangeValid(d)) NoValidationErrors else List(error)
      case Left(_) => List(error)
    }

  def validateWithPath(date: String, path: String): List[MtdError] = validate(date,convertPathToError(path))

  def validateOptionalWithPath(date: Option[String], path: String): List[MtdError] = date match {
    case None       => NoValidationErrors
    case Some(date) => validateWithPath(date, path)
  }


}
