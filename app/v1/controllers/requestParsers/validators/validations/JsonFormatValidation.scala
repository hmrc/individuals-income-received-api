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

import play.api.Logger
import play.api.libs.json._
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}

object JsonFormatValidation {

  private def fromJsPath(path: JsPath): String = path
    .toString()
    .replace("(", "/")
    .replace(")", "")

  def validate[A](data: JsValue, error: MtdError)(implicit reads: Reads[A], writes: Writes[A]): List[MtdError] = {
    if (data == JsObject.empty) List(error) else
      data.validate[A] match {
        case JsSuccess(body, _) =>
          if (Json.toJson(body) == JsObject.empty) List(error) else NoValidationErrors
        case JsError(errors: Seq[(JsPath, Seq[JsonValidationError])]) => errors.flatMap {
          case (path: JsPath, Seq(JsonValidationError(Seq("error.path.missing")))) => {
            Logger.warn(s"[JsonFormatValidation][validate] - " +
              s"A mandatory field is missing the the request body - $path")
            Some(fromJsPath(path))
          }
          case (path: JsPath, Seq(JsonValidationError(Seq(error: String)))) if error.contains("error.expected") => {
            Logger.warn(s"[JsonFormatValidation][validate] - " +
              s"A field has been supplied with the incorrect type - $path")
            Some(fromJsPath(path))
          }
          case _ => None
        } match {
          case Nil => List(error)
          case paths => List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(paths)))
        }
      }
  }
}
