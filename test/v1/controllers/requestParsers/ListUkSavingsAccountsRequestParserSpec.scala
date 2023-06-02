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
import support.UnitSpec
import v1.mocks.validators.MockListUkSavingsAccountsValidator
import v1.models.request.listUkSavingsAccounts.{ListUkSavingsAccountsRawData, ListUkSavingsAccountsRequest}

class ListUkSavingsAccountsRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val savingsAccountId: String       = "someSavingsId"
  implicit val correlationId: String = "someCorrelationId"

  trait Test extends MockListUkSavingsAccountsValidator {

    lazy val parser: ListUkSavingsAccountsRequestParser = new ListUkSavingsAccountsRequestParser(
      validator = mockListUkSavingsAccountValidator
    )

  }

  "parse" should {
    "return the correct request object" when {
      "valid request data is supplied with no savingsAccountId" in new Test {
        val rawData: ListUkSavingsAccountsRawData = ListUkSavingsAccountsRawData(nino = nino, None)
        MockListUkSavingsAccountValidator.validate(rawData) returns Nil

        parser.parseRequest(rawData) shouldBe Right(ListUkSavingsAccountsRequest(Nino(nino), None))
      }

      "valid request data is supplied with a savingsAccountId" in new Test {
        val rawData: ListUkSavingsAccountsRawData = ListUkSavingsAccountsRawData(nino = nino, Some(savingsAccountId))
        MockListUkSavingsAccountValidator.validate(rawData) returns Nil

        parser.parseRequest(rawData) shouldBe Right(ListUkSavingsAccountsRequest(Nino(nino), Some(savingsAccountId)))
      }
    }

    "return an ErrorWrapper" when {
      val rawData = ListUkSavingsAccountsRawData(nino, None)

      "a single validation error occurs" in new Test {
        MockListUkSavingsAccountValidator.validate(rawData) returns List(NinoFormatError)

        parser.parseRequest(rawData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockListUkSavingsAccountValidator.validate(rawData) returns List(NinoFormatError, TaxYearFormatError)

        parser.parseRequest(rawData) shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
