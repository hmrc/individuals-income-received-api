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

package v1.controllers.requestParsers.validators

import v1.controllers.requestParsers.validators.validations.{JsonFormatValidation, NinoValidation, TaxYearValidation, ValueFormatErrorMessages}
import v1.models.errors._
import v1.models.request.amendOtherEmployment._
import v1.controllers.requestParsers.validators.validations._

class AmendOtherEmploymentValidator extends Validator[AmendOtherEmploymentRawData] with ValueFormatErrorMessages {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidator, bodyValueValidator)

  override def validate(data: AmendOtherEmploymentRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: AmendOtherEmploymentRawData => List[List[MtdError]] = (data: AmendOtherEmploymentRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidator: AmendOtherEmploymentRawData => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[AmendOtherEmploymentRequestBody](data.body.json, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyValueValidator: AmendOtherEmploymentRawData => List[List[MtdError]] = { data =>

    val requestBodyData = data.body.json.as[AmendOtherEmploymentRequestBody]

    List(flattenErrors(
      List(
        requestBodyData.shareOption.map(_.zipWithIndex.flatMap {
          case (data, index) => validateShareOption(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.sharesAwardedOrReceived.map(_.zipWithIndex.flatMap {
          case (data, index) => validateSharesAwardedOrReceivedItem(data, index)
        }).getOrElse(NoValidationErrors).toList,
        requestBodyData.disability.map{ data => validateCommonOtherEmployment(data, fieldName = "disability")}.getOrElse(NoValidationErrors),
        requestBodyData.foreignService.map{ data => validateCommonOtherEmployment(data, fieldName = "foreignService")}.getOrElse(NoValidationErrors),
      )
    ))
  }

  private def validateShareOption(shareOptionItem: AmendShareOptionItem, arrayIndex: Int): List[MtdError] = {
    List(
      EmployerNameValidation.validate(shareOptionItem.employerName).map(
        _.copy(paths = Some(Seq(s"/shareOption/$arrayIndex/employerName")))
      ),
      EmployerRefValidation.validateOptional(shareOptionItem.employerRef).map(
        _.copy(paths = Some(Seq(s"/shareOption/$arrayIndex/employerRef")))
      ),
      SchemePlanTypeValidation.validate(shareOptionItem.schemePlanType, false).map(
        _.copy(paths = Some(Seq(s"/shareOption/$arrayIndex/schemePlanType")))
      ),
      DateFormatValidation.validate(
        date = shareOptionItem.dateOfOptionGrant,
        path = s"/shareOption/$arrayIndex/dateOfOptionGrant"
      ),
      DateFormatValidation.validate(
        date = shareOptionItem.dateOfEvent,
        path = s"/shareOption/$arrayIndex/dateOfEvent"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.amountOfConsiderationReceived,
        path = s"/shareOption/$arrayIndex/amountOfConsiderationReceived"
      ),
      BigIntegerValueValidation.validate(
        field = shareOptionItem.noOfSharesAcquired,
        path = s"/shareOption/$arrayIndex/noOfSharesAcquired"
      ),
      ClassOfSharesValidation.validate(shareOptionItem.classOfSharesAcquired, true).map(
        _.copy(paths = Some(Seq(s"/shareOption/$arrayIndex/classOfSharesAcquired")))
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.exercisePrice,
        path = s"/shareOption/$arrayIndex/exercisePrice"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.amountPaidForOption,
        path = s"/shareOption/$arrayIndex/amountPaidForOption"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.marketValueOfSharesOnExcise,
        path = s"/shareOption/$arrayIndex/marketValueOfSharesOnExcise"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.profitOnOptionExercised,
        path = s"/shareOption/$arrayIndex/profitOnOptionExercised"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.employersNicPaid,
        path = s"/shareOption/$arrayIndex/employersNicPaid"
      ),
      DecimalValueValidation.validate(
        amount = shareOptionItem.taxableAmount,
        path = s"/shareOption/$arrayIndex/taxableAmount"
      )
    ).flatten
  }

  private def validateSharesAwardedOrReceivedItem(sharesAwardedOrReceivedItem: AmendSharesAwardedOrReceivedItem, arrayIndex: Int): List[MtdError] = {
    List(
      EmployerNameValidation.validate(sharesAwardedOrReceivedItem.employerName).map(
        _.copy(paths = Some(Seq(s"/sharesAwardedOrReceived/$arrayIndex/employerName")))
      ),
      EmployerRefValidation.validateOptional(sharesAwardedOrReceivedItem.employerRef).map(
        _.copy(paths = Some(Seq(s"/sharesAwardedOrReceived/$arrayIndex/employerRef")))
      ),
      SchemePlanTypeValidation.validate(sharesAwardedOrReceivedItem.schemePlanType, true).map(
        _.copy(paths = Some(Seq(s"/sharesAwardedOrReceived/$arrayIndex/schemePlanType")))
      ),
      DateFormatValidation.validate(
        date = sharesAwardedOrReceivedItem.dateSharesCeasedToBeSubjectToPlan,
        path = s"/sharesAwardedOrReceived/$arrayIndex/dateSharesCeasedToBeSubjectToPlan"
      ),
      BigIntegerValueValidation.validate(
        field = sharesAwardedOrReceivedItem.noOfShareSecuritiesAwarded,
        path = s"/sharesAwardedOrReceived/$arrayIndex/noOfShareSecuritiesAwarded"
      ),
      ClassOfSharesValidation.validate(sharesAwardedOrReceivedItem.classOfShareAwarded, false).map(
        _.copy(paths = Some(Seq(s"/sharesAwardedOrReceived/$arrayIndex/classOfShareAwarded")))
      ),
      DateFormatValidation.validate(
        date = sharesAwardedOrReceivedItem.dateSharesAwarded,
        path = s"/sharesAwardedOrReceived/$arrayIndex/dateSharesAwarded"
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.actualMarketValueOfSharesOnAward,
        path = s"/sharesAwardedOrReceived/$arrayIndex/actualMarketValueOfSharesOnAward"
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.unrestrictedMarketValueOfSharesOnAward,
        path = s"/sharesAwardedOrReceived/$arrayIndex/unrestrictedMarketValueOfSharesOnAward"
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.amountPaidForSharesOnAward,
        path = s"/sharesAwardedOrReceived/$arrayIndex/amountPaidForSharesOnAward"
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.marketValueAfterRestrictionsLifted,
        path = s"/sharesAwardedOrReceived/$arrayIndex/marketValueAfterRestrictionsLifted"
      ),
      DecimalValueValidation.validate(
        amount = sharesAwardedOrReceivedItem.taxableAmount,
        path = s"/sharesAwardedOrReceived/$arrayIndex/taxableAmount"
      )
    ).flatten
  }

  private def validateCommonOtherEmployment(disability: AmendCommonOtherEmployment, fieldName: String): List[MtdError] = {
    List(
      CustomerRefValidation.validateOptional(disability.customerReference).map(
        _.copy(paths = Some(Seq(s"/$fieldName/customerReference")))
      ),
      DecimalValueValidation.validate(
        amount = disability.amountDeducted,
        path = s"/$fieldName/amountDeducted"
      )
    ).flatten
  }


  private def flattenErrors(errors: List[List[MtdError]]): List[MtdError] = {
    errors.flatten.groupBy(_.message).map {case (_, errors) =>

      val baseError = errors.head.copy(paths = Some(Seq.empty[String]))

      errors.fold(baseError)(
        (error1, error2) =>
          error1.copy(paths = Some(error1.paths.getOrElse(Seq.empty[String]) ++ error2.paths.getOrElse(Seq.empty[String])))
      )
    }.toList
  }
}
