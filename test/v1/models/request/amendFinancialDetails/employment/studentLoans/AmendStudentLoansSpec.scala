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

package v1.models.request.amendFinancialDetails.employment.studentLoans

import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.models.request.amendFinancialDetails.emploment.studentLoans.AmendStudentLoans

class AmendStudentLoansSpec extends UnitSpec {

  private val json = Json.parse(
    """
      |{
      |   "uglDeductionAmount": 13343.45,
      |   "pglDeductionAmount": 24242.56
      |}
    """.stripMargin
  )

  private val model = AmendStudentLoans(
    uglDeductionAmount = Some(13343.45),
    pglDeductionAmount = Some(24242.56)
  )

  val emptyJson = Json.parse("""{}""")

  val emptyModel = AmendStudentLoans(None, None)

  "AmendStudentLoans" when {
    "read from valid JSON" should {
      "produce the expected AmendStudentLoans object" in {
        json.as[AmendStudentLoans] shouldBe model
      }
    }

    "read from empty object" should {
      "produce an empty AmendStudentLoans object" in {
        emptyJson.as[AmendStudentLoans] shouldBe emptyModel
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        val invalidJson = Json.parse(
          """
            |{
            |   "uglDeductionAmount": true,
            |   "pglDeductionAmount": 24242.56
            |}
          """.stripMargin
        )

        invalidJson.validate[AmendStudentLoans] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}
