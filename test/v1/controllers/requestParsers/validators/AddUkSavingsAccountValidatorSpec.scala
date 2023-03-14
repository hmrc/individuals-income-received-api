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

package v1.controllers.requestParsers.validators

import api.models.errors._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.models.request.addUkSavingsAccount.AddUkSavingsAccountRawData

class AddUkSavingsAccountValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"

  private val validRequestBodyJson = Json.parse(
    """
      |{
      |  "accountName": "Shares savings account"
      |}
    """.stripMargin
  )

  private val emptyRequestBodyJson = JsObject.empty

  private val nonsenseRequestBodyJson = Json.parse("""{"field": "value"}""")

  private val invalidFieldTypeRequestBodyJson = Json.parse(
    """
      |{
      |  "accountName": []
      |}
    """.stripMargin
  )

  private val invalidValueRequestBodyJson = Json.parse(
    """
      |{
      |  "accountName": ";"
      |}
    """.stripMargin
  )

  private val validRawRequestBody            = AnyContentAsJson(validRequestBodyJson)
  private val emptyRawRequestBody            = AnyContentAsJson(emptyRequestBodyJson)
  private val nonsenseRawRequestBody         = AnyContentAsJson(nonsenseRequestBodyJson)
  private val invalidFieldTypeRawRequestBody = AnyContentAsJson(invalidFieldTypeRequestBodyJson)
  private val invalidValueRawRequestBody     = AnyContentAsJson(invalidValueRequestBodyJson)

  val validator = new AddUkSavingsAccountValidator()

  "AddUkSavingsAccountValidator" when {
    "running a validation" should {
      "return no errors" when {
        "a valid request is supplied" in {
          validator.validate(AddUkSavingsAccountRawData(validNino, validRawRequestBody)) shouldBe Nil
        }
      }

      "return NinoFormatError" when {
        "an invalid nino is supplied" in {
          validator.validate(AddUkSavingsAccountRawData("A12344A", validRawRequestBody)) shouldBe
            List(NinoFormatError)
        }
      }

      "return RuleIncorrectOrEmptyBodyError" when {
        "an empty JSON body is submitted" in {
          validator.validate(AddUkSavingsAccountRawData(validNino, emptyRawRequestBody)) shouldBe
            List(RuleIncorrectOrEmptyBodyError)
        }

        "a non-empty JSON body is submitted without the mandatory account name field" in {
          validator.validate(AddUkSavingsAccountRawData(validNino, nonsenseRawRequestBody)) shouldBe
            List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/accountName"))))
        }

        "account name is supplied with the wrong data type" in {
          validator.validate(AddUkSavingsAccountRawData(validNino, invalidFieldTypeRawRequestBody)) shouldBe
            List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/accountName"))))
        }
      }

      "return AccountNameFormatError" when {
        "an invalid account name is supplied" in {
          validator.validate(AddUkSavingsAccountRawData(validNino, invalidValueRawRequestBody)) shouldBe
            List(AccountNameFormatError)
        }
      }
    }
  }

}
