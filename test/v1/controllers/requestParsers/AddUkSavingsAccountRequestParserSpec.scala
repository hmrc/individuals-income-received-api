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

package v1.controllers.requestParsers

import api.models.domain.Nino
import api.models.errors._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.mocks.validators.MockAddUkSavingsAccountValidator
import v1.models.request.addUkSavingsAccount._

class AddUkSavingsAccountRequestParserSpec extends UnitSpec {

  private val nino                   = "AA123456B"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "accountName": "Shares savings account"
      |}
    """.stripMargin
  )

  private val addUkSavingsAccountRawData = AddUkSavingsAccountRawData(
    nino = nino,
    body = AnyContentAsJson(validRequestBodyJson)
  )

  private val addUkSavingsAccountRequest = AddUkSavingsAccountRequest(
    nino = Nino(nino),
    body = AddUkSavingsAccountRequestBody(accountName = "Shares savings account")
  )

  trait Test extends MockAddUkSavingsAccountValidator {

    lazy val parser: AddUkSavingsAccountRequestParser = new AddUkSavingsAccountRequestParser(
      validator = mockAddUkSavingsAccountValidator
    )

  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAddUkSavingsAccountValidator.validate(addUkSavingsAccountRawData).returns(Nil)
        parser.parseRequest(addUkSavingsAccountRawData) shouldBe Right(addUkSavingsAccountRequest)
      }
    }

    "return an ErrorWrapper" when {
      "a validation error occurs" in new Test {
        MockAddUkSavingsAccountValidator
          .validate(addUkSavingsAccountRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(addUkSavingsAccountRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
    }
  }

}
