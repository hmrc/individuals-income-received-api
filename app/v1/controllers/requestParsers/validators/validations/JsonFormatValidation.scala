/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import play.api.libs.json._
import v1.controllers.requestParsers.validators.Validator
import v1.models.errors.{MissingFieldError, MtdError, WrongFieldTypeError}

object JsonFormatValidation {

  case class PathWrapper(path: JsPath, errorType: MtdError) {
    override def toString: String = path
      .toString()
      .replace("(", "/")
      .replace(")", "")

    val getError: MtdError = this.errorType.copy(paths = Some(List(this.toString)))
  }

  def validate[A](data: JsValue, error: MtdError)(implicit reads: Reads[A], writes: Writes[A]): List[MtdError] = {
    if (data == JsObject.empty) List(error) else
      data.validate[A] match {
        case JsSuccess(body, _) =>
          if (Json.toJson(body) == JsObject.empty) List(error) else NoValidationErrors
        case JsError(errors: Seq[(JsPath, Seq[JsonValidationError])]) => errors.flatMap{
          case (path: JsPath, Seq(JsonValidationError(Seq("error.path.missing")))) => Some(PathWrapper(path, MissingFieldError))
          case (path: JsPath, Seq(JsonValidationError(Seq(error: String)))) if error.contains("error.expected") => Some(PathWrapper(path, WrongFieldTypeError))
          case _ => None
        } match {
          case Nil => List(error)
          case paths => Validator.flattenErrors(paths.map(path => List(path.getError)).toList)
        }
      }
  }
}
