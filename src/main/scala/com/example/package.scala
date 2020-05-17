package com

import eu.timepit.refined._
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.{NonNegative, Positive}
import eu.timepit.refined.types.string.NonEmptyString

package object example {

  implicit class StringOps(text: String) {

    private[this] def toRefinedString[T](implicit evidence: Validate[String, T]): String Refined T = {
      refineV[T](text).fold(error => throw new IllegalArgumentException(error), right => right)
    }

    def toNonEmptyString: NonEmptyString = toRefinedString[NonEmpty]
  }

  implicit class NumericOps[T](number: T)(implicit ev: Numeric[T]) {

    private[this] def toRefinedNumber[R](implicit evidence: Validate[T, R]): T Refined R = {
      refineV[R](number).fold(error => throw new IllegalArgumentException(error), right => right)
    }

    def toPositiveNumber: T Refined Positive = toRefinedNumber[Positive]
    def toNonNegativeNumber: T Refined NonNegative = toRefinedNumber[NonNegative]
  }

}
