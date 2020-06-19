package com.example

import org.scalacheck.Arbitrary
import org.scalacheck._
import eu.timepit.refined.api.RefType
import eu.timepit.refined.string.{MatchesRegex, Url}
import eu.timepit.refined.scalacheck.all._

import scala.reflect.ClassTag
import shapeless.Witness

object Generator {

  implicit def matchesArbitrary[F[_, _], S <: String](implicit referenceType: RefType[F], witness: Witness.Aux[S]): Arbitrary[F[String, MatchesRegex[S]]] =
    arbitraryRefType(Arbitrary {
      import com.mifmif.common.regex.Generex
      new Generex(witness.value).random()
    }.arbitrary)

  implicit def urlArbitrary[F[_, _]](implicit referenceType: RefType[F]):Arbitrary[F[String, Url]] = {
    import org.scalacheck.Gen.{frequency, const}
    arbitraryRefType{
      for {
        prefix <- frequency ( (1, const("file://") ), (1, const("ftp://") ), (2, const("https://") ) , (4, const("http://") ))
        host <- Gen.alphaLowerStr retryUntil (_.length >=10)
        suffix <- frequency ( (4, const(".com") ), (2, const(".org") ), (2, const(".io") ) , (1, const(".me")), (1, const(".dev") ))
      } yield List(prefix, host.take(10), suffix).mkString
    }
  }


  /**
   * Generates a random instances of type T
   *
   * @param arbitrary instance of arbitrary
   * @param classTag  class tag of type T
   * @tparam T type of instance to generate
   * @return
   */
  def random[T](implicit arbitrary: Arbitrary[T], classTag: ClassTag[T]): T = {
    arbitrary.arbitrary.sample.getOrElse(throw new InstantiationException(s"Failed to create an instance of type '${classTag.runtimeClass.getTypeName}'"))
  }
}