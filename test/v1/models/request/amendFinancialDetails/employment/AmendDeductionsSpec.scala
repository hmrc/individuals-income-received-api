/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.request.amendFinancialDetails.employment

import play.api.libs.json.{JsError, JsObject, Json}
import support.UnitSpec
import v1.models.request.amendFinancialDetails.emploment.AmendDeductions
import v1.models.request.amendFinancialDetails.emploment.studentLoans.AmendStudentLoans

class AmendDeductionsSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "studentLoans": {
      |       "uglDeductionAmount": 13343.45,
      |       "pglDeductionAmount": 24242.56
      |   }
      |}
    """.stripMargin
  )

  private val studentLoansModel = AmendStudentLoans(
    uglDeductionAmount = Some(13343.45),
    pglDeductionAmount = Some(24242.56)
  )
  private val deductionsModel = AmendDeductions(
    studentLoans = Some(studentLoansModel)
  )

  "AmendDeductions" when {
    "read from valid JSON" should {
      "produce the expected AmendDeductions object" in {
        json.as[AmendDeductions] shouldBe deductionsModel
      }
    }

    "read from empty JSON" should {
      "produce an empty AmendDeductions object" in {
        val emptyJson = JsObject.empty

        emptyJson.as[AmendDeductions] shouldBe AmendDeductions.empty
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |   "studentLoans": {
            |       "uglDeductionAmount": true,
            |       "pglDeductionAmount": 24242.56
            |   }
            |}
          """.stripMargin
        )

        invalidJson.validate[AmendDeductions] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(deductionsModel) shouldBe json
      }
    }
  }
}