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

package api.support

import api.controllers.EndpointLogContext
import api.models.errors
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{Format, Json}
import support.UnitSpec
import utils.Logging

class DownstreamResponseMappingSupportSpec extends UnitSpec {

  implicit val logContext: EndpointLogContext                = EndpointLogContext("ctrl", "ep")
  val mapping: DownstreamResponseMappingSupport with Logging = new DownstreamResponseMappingSupport with Logging {}

  val correlationId = "someCorrelationId"

  object Error1 extends MtdError("msg", "code1")

  object Error2 extends MtdError("msg", "code2")

  object ErrorBvrMain extends MtdError("msg", "bvrMain")

  object ErrorBvr extends MtdError("msg", "bvr")

  val errorCodeMap: PartialFunction[String, MtdError] = {
    case "ERR1" => Error1
    case "ERR2" => Error2
    case "DS"   => StandardDownstreamError
  }

  case class TestClass(field: Option[String])

  object TestClass {
    implicit val format: Format[TestClass] = Json.format[TestClass]
  }

  "validateRetrieveResponse" when {
    "passed an empty response" should {
      "return a NotFoundError error" in {
        mapping.validateRetrieveResponse(ResponseWrapper(correlationId, TestClass(None))) shouldBe
          Left(ErrorWrapper(correlationId, NotFoundError))
      }
    }
    "passed anything else" should {
      "pass it through" in {
        mapping.validateRetrieveResponse(ResponseWrapper(correlationId, NotFoundError)) shouldBe
          Right(ResponseWrapper(correlationId, NotFoundError))
      }
    }
  }

  "mapping Downstream errors" when {
    "single error" when {
      "the error code is in the map provided" must {
        "use the mapping and wrap" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("ERR1")))) shouldBe
            errors.ErrorWrapper(correlationId, Error1)
        }
      }

      "the error code is not in the map provided" must {
        "default to DownstreamError and wrap" in {
          mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("UNKNOWN")))) shouldBe
            ErrorWrapper(correlationId, StandardDownstreamError)
        }
      }
    }

    "multiple errors" when {
      "the error codes is in the map provided" must {
        "use the mapping and wrap with main error type of BadRequest" in {
          mapping.mapDesErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("ERR2"))))) shouldBe
            errors.ErrorWrapper(correlationId, BadRequestError, Some(Seq(Error1, Error2)))
        }
      }

      "the error code is not in the map provided" must {
        "default main error to DownstreamError ignore other errors" in {
          mapping.mapDesErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("UNKNOWN"))))) shouldBe
            ErrorWrapper(correlationId, StandardDownstreamError)
        }
      }

      "one of the mapped errors is DownstreamError" must {
        "wrap the errors with main error type of DownstreamError" in {
          mapping.mapDesErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("DS"))))) shouldBe
            ErrorWrapper(correlationId, StandardDownstreamError)
        }
      }
    }

    "the error code is an OutboundError" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain))) shouldBe
          errors.ErrorWrapper(correlationId, ErrorBvrMain)
      }
    }

    "the error code is an OutboundError with multiple errors" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDesErrors(errorCodeMap)(ResponseWrapper(correlationId, errors.OutboundError(ErrorBvrMain, Some(Seq(ErrorBvr))))) shouldBe
          errors.ErrorWrapper(correlationId, ErrorBvrMain, Some(Seq(ErrorBvr)))
      }
    }
  }

}
