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

package v1.models.response.retrieveDividends

import config.AppConfig
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import utils.JsonUtils
import play.api.libs.functional.syntax._
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveDividendsResponse(submittedOn: String,
                                     foreignDividend: Option[Seq[ForeignDividendItem]],
                                     dividendIncomeReceivedWhilstAbroad: Option[Seq[DividendIncomeReceivedWhilstAbroadItem]],
                                     stockDividend: Option[StockDividend],
                                     redeemableShares: Option[RedeemableShares],
                                     bonusIssuesOfSecurities: Option[BonusIssuesOfSecurities],
                                     closeCompanyLoansWrittenOff: Option[CloseCompanyLoansWrittenOff]
                                    )

object RetrieveDividendsResponse extends HateoasLinks with JsonUtils {

  implicit val reads: Reads[RetrieveDividendsResponse] = (
    (JsPath \ "submittedOn").read[String] and
    (JsPath \ "foreignDividend").readNullable[Seq[ForeignDividendItem]].mapEmptySeqToNone and
      (JsPath \ "dividendIncomeReceivedWhilstAbroad").readNullable[Seq[DividendIncomeReceivedWhilstAbroadItem]].mapEmptySeqToNone and
      (JsPath \ "stockDividend").readNullable[StockDividend] and
      (JsPath \ "redeemableShares").readNullable[RedeemableShares] and
      (JsPath \ "bonusIssuesOfSecurities").readNullable[BonusIssuesOfSecurities] and
      (JsPath \ "closeCompanyLoansWrittenOff").readNullable[CloseCompanyLoansWrittenOff]
    ) (RetrieveDividendsResponse.apply _)

  implicit val writes: OWrites[RetrieveDividendsResponse] = Json.writes[RetrieveDividendsResponse]

  implicit object RetrieveDividendsLinksFactory extends HateoasLinksFactory[RetrieveDividendsResponse, RetrieveDividendsHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveDividendsHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendDividends(appConfig, nino, taxYear),
        retrieveDividends(appConfig, nino, taxYear),
        deleteDividends(appConfig, nino, taxYear)
      )
    }
  }
}

case class RetrieveDividendsHateoasData(nino: String, taxYear: String) extends HateoasData
