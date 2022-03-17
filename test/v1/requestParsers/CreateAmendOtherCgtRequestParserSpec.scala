/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.requestParsers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.mocks.validators.MockCreateAmendOtherCgtValidator
import api.models.domain.Nino
import api.models.errors._
import v1.models.request.createAmendOtherCgt._

class CreateAmendOtherCgtRequestParserSpec extends UnitSpec {

  val nino: String = "AA123456B"
  val taxYear: String = "2021-22"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "disposals":[
      |    {
      |      "assetType":"otherProperty",
      |      "assetDescription":"Property Sale",
      |      "acquisitionDate":"2021-05-01",
      |      "disposalDate":"2021-06-01",
      |      "disposalProceeds":1000.12,
      |      "allowableCosts":1000.12,
      |      "gain":1000.12,
      |      "claimOrElectionCodes":[
      |        "PRR"
      |      ],
      |      "gainAfterRelief":1000.12,
      |      "rttTaxPaid":1000.12
      |    }
      |  ],
      |  "nonStandardGains":{
      |    "carriedInterestGain":1000.12,
      |    "carriedInterestRttTaxPaid":1000.12,
      |    "attributedGains":1000.12,
      |    "attributedGainsRttTaxPaid":1000.12,
      |    "otherGains":1000.12,
      |    "otherGainsRttTaxPaid":1000.12
      |  },
      |  "losses":{
      |    "broughtForwardLossesUsedInCurrentYear":1000.12,
      |    "setAgainstInYearGains":1000.12,
      |    "setAgainstInYearGeneralIncome":1000.12,
      |    "setAgainstEarlierYear":1000.12
      |  },
      |  "adjustments":1000.12
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)



  private val validRequestBodyModel = CreateAmendOtherCgtRequestBody(
    disposals = Some(
      Seq(
        Disposal(
          assetType = "otherProperty",
          assetDescription = "Property Sale",
          acquisitionDate = "2021-05-01",
          disposalDate = "2021-06-01",
          disposalProceeds = 1000.12,
          allowableCosts = 1000.12,
          gain = Some(1000.12),
          loss = None,
          claimOrElectionCodes = Some(Seq("PRR")),
          gainAfterRelief = Some(1000.12),
          lossAfterRelief = None,
          rttTaxPaid = Some(1000.12)
        )
      )
    ),
    nonStandardGains = Some(
      NonStandardGains(
        carriedInterestGain = Some(1000.12),
        carriedInterestRttTaxPaid = Some(1000.12),
        attributedGains = Some(1000.12),
        attributedGainsRttTaxPaid = Some(1000.12),
        otherGains = Some(1000.12),
        otherGainsRttTaxPaid = Some(1000.12)
      )
    ),
    losses = Some(
      Losses(
        broughtForwardLossesUsedInCurrentYear = Some(1000.12),
        setAgainstInYearGains = Some(1000.12),
        setAgainstInYearGeneralIncome = Some(1000.12),
        setAgainstEarlierYear = Some(1000.12)
      )
    )   ,
    adjustments = Some(1000.12)
  )

  private val createAmendOtherCgtRawData = CreateAmendOtherCgtRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockCreateAmendOtherCgtValidator {
    lazy val parser: CreateAmendOtherCgtRequestParser = new CreateAmendOtherCgtRequestParser(
      validator = mockCreateAmendOtherCgtValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateAmendOtherCgtValidator.validate(createAmendOtherCgtRawData).returns(Nil)

        parser.parseRequest(createAmendOtherCgtRawData) shouldBe
          Right(CreateAmendOtherCgtRequest(Nino(nino), taxYear, validRequestBodyModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockCreateAmendOtherCgtValidator.validate(createAmendOtherCgtRawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(createAmendOtherCgtRawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockCreateAmendOtherCgtValidator.validate(createAmendOtherCgtRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(createAmendOtherCgtRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }
}