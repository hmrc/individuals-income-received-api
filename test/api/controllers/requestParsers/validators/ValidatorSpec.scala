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

package api.controllers.requestParsers.validators

import api.models.errors.{MtdError, NinoFormatError, NotFoundError}
import api.models.request.RawData
import org.scalamock.scalatest.MockFactory
import play.api.http.Status.BAD_REQUEST
import support.UnitSpec

import scala.collection.immutable.Seq

class ValidatorSpec extends UnitSpec with MockFactory {

  private trait Test {
    val validator = new TestValidator()
  }

  "running a validation" should {
    "return no errors" when {
      "when all data is correct " in new Test {

        // Set up the mock validations
        val levelOneValidationOne = new MockFunctionObject("Level: 1    Validation 1")
        val levelOneValidationTwo = new MockFunctionObject("Level: 1    Validation 2")

        def levelOneValidations: TestRawData => Seq[Seq[MtdError]] = (_: TestRawData) => {
          List(
            levelOneValidationOne.validate(shouldError = false, None),
            levelOneValidationTwo.validate(shouldError = false, None)
          )
        }

        val validationSet = List(levelOneValidations)

        val inputData: TestRawData = TestRawData("ABCDEF", "12345")
        val result                 = validator.run(validationSet, inputData)
        result.isEmpty shouldBe true
        levelOneValidationOne.called shouldBe 1
        levelOneValidationTwo.called shouldBe 1

      }
    }

    "return a list of validation errors on level one" when {
      "when there are failed validations " in new Test {
        // Set up the mock validations
        val levelOneValidationOne = new MockFunctionObject("Level: 1    Validation 1")
        val levelOneValidationTwo = new MockFunctionObject("Level: 1    Validation 2")
        val mockError: MtdError   = MtdError("MOCK", "SOME ERROR", BAD_REQUEST)

        def levelOneValidations: TestRawData => Seq[Seq[MtdError]] = (_: TestRawData) => {
          List(
            levelOneValidationOne.validate(shouldError = false, None),
            levelOneValidationTwo.validate(shouldError = true, Some(mockError))
          )
        }

        val validationSet = List(levelOneValidations)

        val inputData: TestRawData = TestRawData("ABCDEF", "12345")
        val result                 = validator.run(validationSet, inputData)
        result.isEmpty shouldBe false
        result.size shouldBe 1
        result.head shouldBe mockError
        levelOneValidationOne.called shouldBe 1
        levelOneValidationTwo.called shouldBe 1
      }
    }

    "return a list of validation errors on level two" when {
      "when there are failed validations only on level 2 " in new Test {
        // Set up the mock validations
        val levelOneValidationOne = new MockFunctionObject("Level: 1    Validation 1")
        val levelOneValidationTwo = new MockFunctionObject("Level: 1    Validation 2")
        val levelTwoValidationOne = new MockFunctionObject("Level: 2    Validation 1")
        val levelTwoValidationTwo = new MockFunctionObject("Level: 2    Validation 2")
        val mockError: MtdError   = MtdError("MOCK", "SOME ERROR ON LEVEL 2", BAD_REQUEST)

        def levelOneValidations: TestRawData => Seq[Seq[MtdError]] = (_: TestRawData) => {
          List(
            levelOneValidationOne.validate(shouldError = false, None),
            levelOneValidationTwo.validate(shouldError = false, None)
          )
        }

        def levelTwoValidations: TestRawData => Seq[Seq[MtdError]] = (_: TestRawData) => {
          List(
            levelTwoValidationOne.validate(shouldError = false, None),
            levelTwoValidationTwo.validate(shouldError = true, Some(mockError))
          )
        }

        val validationSet = List(levelOneValidations, levelTwoValidations)

        val inputData: TestRawData = TestRawData("ABCDEF", "12345")
        val result                 = validator.run(validationSet, inputData)
        result.isEmpty shouldBe false
        result.size shouldBe 1
        result.head shouldBe mockError
        levelOneValidationOne.called shouldBe 1
        levelOneValidationTwo.called shouldBe 1
        levelTwoValidationOne.called shouldBe 1
        levelTwoValidationTwo.called shouldBe 1
      }
    }
  }

  "flattenErrors" should {
    "combine errors of the same type" in new Test {
      val errors: List[List[MtdError]] = List(
        List(NotFoundError),
        List(NinoFormatError.copy(paths = Some(Seq("one")))),
        List(NinoFormatError.copy(paths = Some(Seq("two"))))
      )

      val flatErrors: List[MtdError] = List(
        NotFoundError,
        NinoFormatError.copy(paths = Some(Seq("one", "two")))
      )

      validator.flattenErrors(errors) shouldBe flatErrors
    }

    "return the input for a list of unique errors" in new Test {
      val errors: List[List[MtdError]] = List(
        List(NotFoundError),
        List(NinoFormatError.copy(paths = Some(Seq("one"))))
      )

      validator.flattenErrors(errors) shouldBe errors.flatten
    }

    "handle empty lists correctly" in new Test {
      val emptyErrorList: List[List[MtdError]]   = List.empty[List[MtdError]]
      val listOfEmptyLists: List[List[MtdError]] = List(List.empty[MtdError])
      validator.flattenErrors(emptyErrorList) shouldBe List.empty[MtdError]
      validator.flattenErrors(listOfEmptyLists) shouldBe List.empty[MtdError]
    }
  }

  class MockFunctionObject(name: String) {
    var called = 0

    def validate(shouldError: Boolean, errorToReturn: Option[MtdError]): List[MtdError] = {
      called = called + 1
      if (shouldError) List(errorToReturn.get) else List()
    }

  }

  private case class TestRawData(fieldOne: String, fieldTwo: String) extends RawData

// Create a Validator based off the trait to be able to test it
  private class TestValidator extends Validator[TestRawData] {

    override def validate(data: TestRawData): scala.Seq[MtdError] = {
      run(List(), data) match {
        case Nil        => List()
        case err :: Nil => List(err)
        case errs       => errs
      }
    }

  }

}