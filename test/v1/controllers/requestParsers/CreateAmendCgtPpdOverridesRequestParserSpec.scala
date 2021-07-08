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

package v1.controllers.requestParsers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.mocks.validators.MockCreateAmendCgtPpdOverridesValidator
import v1.models.domain.Nino
import v1.models.errors.{BadRequestError, DateFormatError, ErrorWrapper, NinoFormatError, SubmissionIdFormatError, ValueFormatError}
import v1.models.request.createAmendCgtPpdOverrides._

class CreateAmendCgtPpdOverridesRequestParserSpec extends UnitSpec {

  val nino: String = "AA123456B"
  val taxYear: String = "2019-20"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |    "multiplePropertyDisposals": [
      |         {
      |            "submissionId": "AB0000000092",
      |            "amountOfNetGain": 1234.78
      |         },
      |         {
      |            "submissionId": "AB0000000098",
      |            "amountOfNetLoss": 134.99
      |         }
      |    ],
      |    "singlePropertyDisposals": [
      |         {
      |             "submissionId": "AB0000000098",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23,
      |             "amountOfNetGain": 4567.89
      |         },
      |         {
      |             "submissionId": "AB0000000091",
      |             "completionDate": "2020-02-28",
      |             "disposalProceeds": 454.24,
      |             "acquisitionDate": "2020-03-29",
      |             "acquisitionAmount": 3434.45,
      |             "improvementCosts": 233.45,
      |             "additionalCosts": 423.34,
      |             "prfAmount": 2324.67,
      |             "otherReliefAmount": 3434.23,
      |             "lossesFromThisYear": 436.23,
      |             "lossesFromPreviousYear": 234.23,
      |             "amountOfNetLoss": 4567.89
      |         }
      |    ]
      |}
      |""".stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)

  private val multiplePropertyDisposalsModels: Seq[MultiplePropertyDisposals] =
    Seq(
      MultiplePropertyDisposals(
        "AB0000000092",
        Some(1234.78),
        None
      ),
      MultiplePropertyDisposals(
        "AB0000000098",
        None,
        Some(134.99)
      )
    )

  private val singlePropertyDisposalsModels: Seq[SinglePropertyDisposals] =
    Seq(
      SinglePropertyDisposals(
        "AB0000000098",
        "2020-02-28",
        454.24,
        "2020-03-29",
        3434.45,
        233.45,
        423.34,
        2324.67,
        3434.23,
        Some(436.23),
        Some(234.23),
        Some(4567.89),
        None
      ),
      SinglePropertyDisposals(
        "AB0000000091",
        "2020-02-28",
        454.24,
        "2020-03-29",
        3434.45,
        233.45,
        423.34,
        2324.67,
        3434.23,
        Some(436.23),
        Some(234.23),
        None,
        Some(4567.89)
      )
    )

  private val requestBody: CreateAmendCgtPpdOverridesRequestBody =
    CreateAmendCgtPpdOverridesRequestBody(
      Some(multiplePropertyDisposalsModels),
      Some(singlePropertyDisposalsModels)
    )

  private val createAmendCgtPpdOverridesRawData = CreateAmendCgtPpdOverridesRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockCreateAmendCgtPpdOverridesValidator {
    lazy val parser: CreateAmendCgtPpdOverridesRequestParser = new CreateAmendCgtPpdOverridesRequestParser(
      validator = mockCreateAmendCgtPpdValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateAmendCgtPpdOverridesValidator.validate(createAmendCgtPpdOverridesRawData).returns(Nil)

        parser.parseRequest(createAmendCgtPpdOverridesRawData) shouldBe
        Right(CreateAmendCgtPpdOverridesRequest(Nino(nino), taxYear, requestBody))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation occurs" in new Test {
        MockCreateAmendCgtPpdOverridesValidator.validate(
          createAmendCgtPpdOverridesRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(createAmendCgtPpdOverridesRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))

      }

      "multiple validation errors occur" in new Test {

        private val allInvalidValueBodyJson: JsValue = Json.parse(
          """
            |{
            |    "multiplePropertyDisposals": [
            |         {
            |            "submissionId": "notASubmissionId",
            |            "amountOfNetGain": 1234.787385
            |         },
            |         {
            |            "submissionId": "notASubmissionId",
            |            "amountOfNetLoss": -134.99
            |         }
            |    ],
            |    "singlePropertyDisposals": [
            |         {
            |             "submissionId": "notASubmissionId",
            |             "completionDate": "20201-02-28",
            |             "disposalProceeds": 454.24999,
            |             "acquisitionDate": "20209-03-29",
            |             "acquisitionAmount": 3434.45346,
            |             "improvementCosts": 233.4628,
            |             "additionalCosts": 423.34829,
            |             "prfAmount": -2324.67,
            |             "otherReliefAmount": -3434.23,
            |             "lossesFromThisYear": 436.23297423,
            |             "lossesFromPreviousYear": 234.2334728,
            |             "amountOfNetGain": -4567.89
            |         },
            |         {
            |             "submissionId": "notASubmissionId",
            |             "completionDate": "20-02-28",
            |             "disposalProceeds": -454.24,
            |             "acquisitionDate": "200-03-29",
            |             "acquisitionAmount": 3434.45837,
            |             "improvementCosts": -233.45,
            |             "additionalCosts": -423.34,
            |             "prfAmount": 2324.678372,
            |             "otherReliefAmount": -3434.23,
            |             "lossesFromThisYear": 436.23287,
            |             "lossesFromPreviousYear": -234.23,
            |             "amountOfNetLoss": 4567.8983724
            |         }
            |    ]
            |}
            |""".stripMargin
        )

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueBodyJson)

        private val allInvalidValueErrors = List(
          SubmissionIdFormatError.copy(
            paths = Some(Seq("/multiplePropertyDisposals/1/submissionId",
              "/singlePropertyDisposals/1/submissionId",
              "/multiplePropertyDisposals/2/submissionId",
              "/singlePropertyDisposals/2/submissionId"))
          ),
          ValueFormatError.copy(
            paths = Some(List(
              "/multiplePropertyDisposals/1/amountOfNetGain",
              "/multiplePropertyDisposals/2/amountOfNetLoss",
              "/singlePropertyDisposals/1/amountOfNetLoss",
              "/singlePropertyDisposals/1/disposalProceeds",
              "/singlePropertyDisposals/1/acquisitionAmount",
              "/singlePropertyDisposals/1/improvementCosts",
              "/singlePropertyDisposals/1/additionalCosts",
              "/singlePropertyDisposals/1/prfAmount",
              "/singlePropertyDisposals/1/otherReliefAmount",
              "/singlePropertyDisposals/1/lossesFromThisYear",
              "/singlePropertyDisposals/1/lossesFromPreviousYear",
              "/singlePropertyDisposals/1/amountOfNetGain",
              "/singlePropertyDisposals/2/disposalProceeds",
              "/singlePropertyDisposals/2/acquisitionAmount",
              "/singlePropertyDisposals/2/improvementCosts",
              "/singlePropertyDisposals/2/improvementCosts",
              "/singlePropertyDisposals/2/additionalCosts",
              "/singlePropertyDisposals/2/prfAmount",
              "/singlePropertyDisposals/2/otherReliefAmount",
              "/singlePropertyDisposals/2/lossesFromThisYear",
              "/singlePropertyDisposals/2/lossesFromPreviousYear",
              "/singlePropertyDisposals/2/amountOfNetLoss"
            ))
          ),
          DateFormatError.copy(
            paths = Some(List(
              "singlePropertyDisposals/1/completionDate",
              "singlePropertyDisposals/1/acquisitionDate",              "singlePropertyDisposals/1/completionDate",
              "singlePropertyDisposals/2/completionDate",              "singlePropertyDisposals/1/completionDate",
              "singlePropertyDisposals/2/acquisitionDate",
            ))
          )
        )

        MockCreateAmendCgtPpdOverridesValidator.validate(createAmendCgtPpdOverridesRawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(createAmendCgtPpdOverridesRawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }
}
