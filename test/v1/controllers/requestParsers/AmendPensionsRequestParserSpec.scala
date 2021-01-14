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
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockAmendPensionsValidator
import v1.models.errors._
import v1.models.request.amendPensions._

class AmendPensionsRequestParserSpec extends UnitSpec{

  val nino: String = "AA123456B"
  val taxYear: String = "2020-21"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignPensions": [
      |      {
      |         "countryCode": "DEU",
      |         "amountBeforeTax": 100.23,
      |         "taxTakenOff": 1.23,
      |         "specialWithholdingTax": 2.23,
      |         "foreignTaxCreditRelief": false,
      |         "taxableAmount": 3.23
      |      },
      |      {
      |         "countryCode": "FRA",
      |         "amountBeforeTax": 200.25,
      |         "taxTakenOff": 1.27,
      |         "specialWithholdingTax": 2.50,
      |         "foreignTaxCreditRelief": true,
      |         "taxableAmount": 3.50
      |      }
      |   ],
      |   "overseasPensionContributions": [
      |      {
      |         "customerReference": "PENSIONINCOME245",
      |         "exemptEmployersPensionContribs": 200.23,
      |         "migrantMemReliefQopsRefNo": "QOPS000000",
      |         "dblTaxationRelief": 4.23,
      |         "dblTaxationCountryCode": "FRA",
      |         "dblTaxationArticle": "AB3211-1",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-123456"
      |      },
      |      {
      |         "customerReference": "PENSIONINCOME275",
      |         "exemptEmployersPensionContribs": 270.50,
      |         "migrantMemReliefQopsRefNo": "QOPS000245",
      |         "dblTaxationRelief": 5.50,
      |         "dblTaxationCountryCode": "NGA",
      |         "dblTaxationArticle": "AB3477-5",
      |         "dblTaxationTreaty": "Treaty",
      |         "sf74reference": "SF74-1235"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)

  private val fullForeignPensionsModel = Seq(
    AmendForeignPensionsItem(
      countryCode = "DEU",
      amountBeforeTax = Some(100.23),
      taxTakenOff = Some(1.23),
      specialWithholdingTax = Some(2.23),
      foreignTaxCreditRelief = false,
      taxableAmount = 3.23
    ),
    AmendForeignPensionsItem(
      countryCode = "FRA",
      amountBeforeTax = Some(200.25),
      taxTakenOff = Some(1.27),
      specialWithholdingTax = Some(2.50),
      foreignTaxCreditRelief = true,
      taxableAmount = 3.50
    )
  )

  private val fullOverseasPensionContributionsModel = Seq(
    AmendOverseasPensionContributionsItem(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 200.23,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(4.23),
      dblTaxationCountryCode = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-123456")
    ),
    AmendOverseasPensionContributionsItem(
      customerReference = Some("PENSIONINCOME275"),
      exemptEmployersPensionContribs = 270.50,
      migrantMemReliefQopsRefNo = Some("QOPS000245"),
      dblTaxationRelief = Some(5.50),
      dblTaxationCountryCode = Some("NGA"),
      dblTaxationArticle = Some("AB3477-5"),
      dblTaxationTreaty = Some("Treaty"),
      sf74reference = Some("SF74-1235")
    )
  )

  private val validRequestBodyModel = AmendPensionsRequestBody(
    Some(fullForeignPensionsModel),
    Some(fullOverseasPensionContributionsModel)
  )

  private val amendPensionsRawData = AmendPensionsRawData(
    nino = nino,
    taxYear = taxYear,
    body = validRawRequestBody
  )

  trait Test extends MockAmendPensionsValidator {
    lazy val parser: AmendPensionsRequestParser = new AmendPensionsRequestParser(
      validator = mockAmendPensionsValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendPensionsValidator.validate(amendPensionsRawData).returns(Nil)

        parser.parseRequest(amendPensionsRawData) shouldBe
          Right(AmendPensionsRequest(Nino(nino), taxYear, validRequestBodyModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendPensionsValidator.validate(amendPensionsRawData.copy(nino = "notANino"))
          .returns(List(NinoFormatError))

        parser.parseRequest(amendPensionsRawData.copy(nino = "notANino")) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple path parameter validation errors occur" in new Test {
        MockAmendPensionsValidator.validate(amendPensionsRawData.copy(nino = "notANino", taxYear = "notATaxYear"))
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(amendPensionsRawData.copy(nino = "notANino", taxYear = "notATaxYear")) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }

      "multiple field value validation errors occur" in new Test {

        private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
          """
            |{
            |   "foreignPensions": [
            |      {
            |         "countryCode": "SBT",
            |         "amountBeforeTax": 100.234,
            |         "taxTakenOff": 1.235,
            |         "specialWithholdingTax": -2.23,
            |         "foreignTaxCreditRelief": false,
            |         "taxableAmount": -3.23
            |      },
            |      {
            |         "countryCode": "FRANCE",
            |         "amountBeforeTax": -200.25,
            |         "taxTakenOff": 1.273,
            |         "specialWithholdingTax": -2.50,
            |         "foreignTaxCreditRelief": true,
            |         "taxableAmount": 3.508
            |      }
            |   ],
            |   "overseasPensionContributions": [
            |      {
            |         "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |         "exemptEmployersPensionContribs": 200.237,
            |         "migrantMemReliefQopsRefNo": "This qopsRef string is 91 characters long ---------------------------------------------- 91",
            |         "dblTaxationRelief": -4.238,
            |         "dblTaxationCountryCode": "PUR",
            |         "dblTaxationArticle": "This dblTaxationArticle string is 91 characters long ------------------------------------91",
            |         "dblTaxationTreaty": "This dblTaxationTreaty string is 91 characters long -------------------------------------91",
            |         "sf74reference": "This sf74Ref string is 91 characters long ---------------------------------------------- 91"
            |      },
            |      {
            |         "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
            |         "exemptEmployersPensionContribs": -270.509,
            |         "migrantMemReliefQopsRefNo": "This qopsRef string is 91 characters long ---------------------------------------------- 91",
            |         "dblTaxationRelief": 5.501,
            |         "dblTaxationCountryCode": "GERMANY",
            |         "dblTaxationArticle": "This dblTaxationArticle string is 91 characters long ------------------------------------91",
            |         "dblTaxationTreaty": "This dblTaxationTreaty string is 91 characters long -------------------------------------91",
            |         "sf74reference": "This sf74Ref string is 91 characters long ---------------------------------------------- 91"
            |      }
            |   ]
            |}
          """.stripMargin
        )

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

        private val allInvalidValueErrors = List(
          CustomerRefFormatError.copy(
            paths = Some(List(
              "/overseasPensionContributions/0/customerReference",
              "/overseasPensionContributions/1/customerReference"
            ))
          ),
          QOPSRefFormatError.copy(
            paths = Some(List(
              "/overseasPensionContributions/0/migrantMemReliefQopsRefNo",
              "/overseasPensionContributions/1/migrantMemReliefQopsRefNo"
            ))
          ),
          SF74RefFormatError.copy(
            paths = Some(List(
              "/overseasPensionContributions/0/sf74reference",
              "/overseasPensionContributions/1/sf74reference"
            ))
          ),
          DoubleTaxationTreatyFormatError.copy(
            paths = Some(List(
              "/overseasPensionContributions/0/dblTaxationTreaty",
              "/overseasPensionContributions/1/dblTaxationTreaty"
            ))
          ),
          CountryCodeRuleError.copy(
            paths = Some(List(
              "/foreignPensions/0/countryCode",
              "/overseasPensionContributions/0/dblTaxationCountryCode"
            ))
          ),
          DoubleTaxationArticleFormatError.copy(
            paths = Some(List(
              "/overseasPensionContributions/0/dblTaxationArticle",
              "/overseasPensionContributions/1/dblTaxationArticle"
            ))
          ),
          CountryCodeFormatError.copy(
            paths = Some(List(
              "/foreignPensions/1/countryCode",
              "/overseasPensionContributions/1/dblTaxationCountryCode"
            ))
          ),
          ValueFormatError.copy(
            message = "The field should be between 0 and 99999999999.99",
            paths = Some(List(
              "/foreignPensions/0/amountBeforeTax",
              "/foreignPensions/0/taxTakenOff",
              "/foreignPensions/0/specialWithholdingTax",
              "/foreignPensions/0/taxableAmount",
              "/foreignPensions/1/amountBeforeTax",
              "/foreignPensions/1/taxTakenOff",
              "/foreignPensions/1/specialWithholdingTax",
              "/foreignPensions/1/taxableAmount",
              "/overseasPensionContributions/0/exemptEmployersPensionContribs",
              "/overseasPensionContributions/0/dblTaxationRelief",
              "/overseasPensionContributions/1/exemptEmployersPensionContribs",
              "/overseasPensionContributions/1/dblTaxationRelief"
            ))
          )
        )

        MockAmendPensionsValidator.validate(amendPensionsRawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)

        parser.parseRequest(amendPensionsRawData.copy(body = allInvalidValueRawRequestBody)) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(allInvalidValueErrors)))
      }
    }
  }
}