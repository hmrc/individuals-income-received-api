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

package v1r6.models.errors

import play.api.libs.json.{Json, OWrites}

case class MtdError(code: String, message: String, paths: Option[Seq[String]] = None) {

  def withExtraPath(newPath: String): MtdError = paths.fold(this.copy(paths = Some(Seq(newPath)))) { existingPaths =>
    this.copy(paths = Some(existingPaths :+ newPath))
  }
}

object MtdError {
  implicit val writes: OWrites[MtdError] = Json.writes[MtdError]

  implicit def genericWrites[T <: MtdError]: OWrites[T] =
    writes.contramap[T](c => c: MtdError)
}

object CustomMtdError {
  def unapply(arg: MtdError): Option[String] = Some(arg.code)
}

object NinoFormatError                  extends MtdError("FORMAT_NINO", "The provided NINO is invalid")
object TaxYearFormatError               extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid")
object EmploymentIdFormatError          extends MtdError("FORMAT_EMPLOYMENT_ID", "The provided employment ID is invalid")
object PpdSubmissionIdFormatError       extends MtdError("FORMAT_PPD_SUBMISSION_ID", "The provided ppdSubmissionId is invalid")
object CountryCodeFormatError           extends MtdError("FORMAT_COUNTRY_CODE", "The format of the country code is invalid")
object CountryCodeRuleError             extends MtdError("RULE_COUNTRY_CODE", "The country code is not a valid ISO 3166-1 alpha-3 country code")
object ValueFormatError                 extends MtdError("FORMAT_VALUE", "The value must be between 0.00 and 99999999999.99")
object CustomerRefFormatError           extends MtdError("FORMAT_CUSTOMER_REF", "The provided customer reference is invalid")
object EventFormatError                 extends MtdError("FORMAT_EVENT", "The provided policy event is invalid")
object QOPSRefFormatError               extends MtdError("FORMAT_QOPS_REF", "The provided QOPS reference number is invalid")
object DoubleTaxationArticleFormatError extends MtdError("FORMAT_DOUBLE_TAXATION_ARTICLE", "The provided double taxation article is invalid")
object DoubleTaxationTreatyFormatError  extends MtdError("FORMAT_DOUBLE_TAXATION_TREATY", "The provided double taxation treaty is invalid")
object SF74RefFormatError               extends MtdError("FORMAT_SF74_REF", "The provided SF74 reference is invalid")
object EmployerNameFormatError          extends MtdError("FORMAT_EMPLOYER_NAME", "The provided employer name is invalid")
object EmployerRefFormatError           extends MtdError("FORMAT_EMPLOYER_REF", "The provided employer ref is invalid")
object DateFormatError                  extends MtdError("FORMAT_DATE", "The field should be in the format YYYY-MM-DD")
object ClassOfSharesAwardedFormatError  extends MtdError("FORMAT_CLASS_OF_SHARES_AWARDED", "The provided class of shares awarded is invalid")
object ClassOfSharesAcquiredFormatError extends MtdError("FORMAT_CLASS_OF_SHARES_ACQUIRED", "The provided class of shares acquired is invalid")
object SchemePlanTypeFormatError        extends MtdError("FORMAT_SCHEME_PLAN_TYPE", "The provided scheme plan type is invalid")
object PayrollIdFormatError             extends MtdError("FORMAT_PAYROLL_ID", "The provided payroll ID is invalid")
object StartDateFormatError             extends MtdError("FORMAT_START_DATE", "The provided start date is invalid")
object CessationDateFormatError         extends MtdError("FORMAT_CESSATION_DATE", "The provided cessation date is invalid")
object SourceFormatError                extends MtdError("FORMAT_SOURCE", "The provided source is invalid")
object AssetDescriptionFormatError      extends MtdError("FORMAT_ASSET_DESCRIPTION", "The provided asset description is invalid")
object AssetTypeFormatError             extends MtdError("FORMAT_ASSET_TYPE", "The format of the assetType value is invalid")
object ClaimOrElectionCodesFormatError  extends MtdError("FORMAT_CLAIM_OR_ELECTION_CODES", "The format of the claimOrElectionCodes value is invalid")

// Rule Errors
object RuleTaxYearNotSupportedError
  extends MtdError(
    code = "RULE_TAX_YEAR_NOT_SUPPORTED",
    message = "The specified tax year is not supported. That is, the tax year specified is before the minimum tax year value"
  )

object RuleIncorrectOrEmptyBodyError
  extends MtdError(code = "RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", message = "An empty or non-matching body was submitted")

object RuleTaxYearRangeInvalidError
  extends MtdError(
    code = "RULE_TAX_YEAR_RANGE_INVALID",
    message = "Tax year range invalid. A tax year range of one year is required"
  )

object RuleTaxYearNotEndedError
  extends MtdError(
    code = "RULE_TAX_YEAR_NOT_ENDED",
    message = "Tax year not ended"
  )

object RuleCessationDateBeforeStartDateError
  extends MtdError(
    code = "RULE_CESSATION_DATE_BEFORE_START_DATE",
    message = "The cessation date cannot be earlier than the start date"
  )

object RuleStartDateAfterTaxYearEndError
  extends MtdError(
    code = "RULE_START_DATE_AFTER_TAX_YEAR_END",
    message = "The start date cannot be later than the tax year end"
  )

object RuleCessationDateBeforeTaxYearStartError
  extends MtdError(
    code = "RULE_CESSATION_DATE_BEFORE_TAX_YEAR_START",
    message = "The cessation date cannot be before the tax year starts"
  )

object RuleUpdateForbiddenError
  extends MtdError(
    code = "RULE_UPDATE_FORBIDDEN",
    message = "The update for an HMRC held employment is not permitted"
  )

object RuleCustomEmploymentError
  extends MtdError(
    code = "RULE_CUSTOM_EMPLOYMENT",
    message = "A custom employment cannot be ignored"
  )

object RuleCustomEmploymentUnignoreError
  extends MtdError(
    code = "RULE_CUSTOM_EMPLOYMENT",
    message = "A custom employment cannot be unignored"
  )

object RuleLumpSumsError
  extends MtdError(
    code = "RULE_LUMP_SUMS",
    message = "At least one child object is required when lumpSums are provided"
  )

object RuleAmountGainLossError
  extends MtdError(
    code = "RULE_AMOUNT_GAIN_LOSS",
    message = "Either amountOfGain or amountOfLoss, must be provided but not both"
  )

object RuleLossesGreaterThanGainError
  extends MtdError(
    code = "RULE_LOSSES_GREATER_THAN_GAIN",
    message = "The total losses from this year or the previous year must not be greater than the gain"
  )

object RuleGainLossError
    extends MtdError(
      code = "RULE_GAIN_LOSS",
      message = "Only one of gain or loss values can be provided"
    )

object RuleDisposalDateError
    extends MtdError(
      code = "RULE_DISPOSAL_DATE",
      message = ""
    )

object RuleAcquisitionDateError
    extends MtdError(
      code = "RULE_ACQUISITION_DATE",
      message = "The acquisitionDate must not be later than disposalDate"
    )

object RuleGainAfterReliefLossAfterReliefError
    extends MtdError(
      code = "RULE_GAIN_AFTER_RELIEF_LOSS_AFTER_RELIEF",
      message = "Only one of gainAfterRelief or lossAfterRelief values can be provided"
    )

object RuleCompletionDateBeforeDisposalDateError  extends MtdError("RULE_COMPLETION_DATE_BEFORE_DISPOSAL_DATE", "The completionDate must not be earlier than the disposalDate")

object RuleAcquisitionDateAfterDisposalDateError  extends MtdError("RULE_ACQUISITION_DATE_AFTER_DISPOSAL_DATE", "The acquisitionDate must not be later than disposalDate")

object RuleCompletionDateError  extends MtdError("RULE_COMPLETION_DATE", "The completionDate must be within the specific tax year and not in the future. If the specified tax year has not ended, the completionDate must be between 7th March and 5th April")

object RuleDuplicatedPpdSubmissionIdError
  extends MtdError(
    code = "RULE_DUPLICATED_PPD_SUBMISSION_ID",
    message = "A provided ppdSubmissionId is duplicated"
  )

object RuleIncorrectDisposalTypeError
  extends MtdError(
    code = "RULE_INCORRECT_DISPOSAL_TYPE",
    message = "A provided ppdSubmissionId is being used for the incorrect disposal type"
  )

// Not found errors
object PpdSubmissionIdNotFoundError
    extends MtdError(
      code = "PPD_SUBMISSION_ID_NOT_FOUND",
      message = "Matching resource not found"
    )

// Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")

object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error")

object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")

// Authorisation Errors
object UnauthorisedError       extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised")
object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized")

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found")

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body")
