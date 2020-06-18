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

package v1.models.hateoas

object RelType {
  val AMEND_SAVINGS_INCOME = "amend-savings-income"
  val DELETE_SAVINGS_INCOME = "delete-savings-income"
  val AMEND_INSURANCE_POLICIES_INCOME = "amend-insurance-policies-income"
  val DELETE_INSURANCE_POLICIES_INCOME = "delete-insurance-policies-income"
  val AMEND_FOREIGN_INCOME = "amend-foreign-income"
  val DELETE_FOREIGN_INCOME = "delete-foreign-income"
  val AMEND_PENSIONS_INCOME = "amend-pensions-income"
  val DELETE_PENSIONS_INCOME = "delete-pensions-income"

  val SELF = "self"
}
