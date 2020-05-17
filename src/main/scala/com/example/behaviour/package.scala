package com.example

import java.time.LocalDate

import cats.data.NonEmptyList
import com.example.domain.{Event, News}
import com.example.generic.TupleGeneric
import shapeless.HList

package object behaviour {

  implicit class PairOps(pair: Tuple2[LocalDate, NonEmptyList[News]]) {
    import shapeless._, labelled._, record._, ops.record._, syntax.singleton._
    def toCaseClass[E<:Event] (implicit labelledGeneric: LabelledGeneric[E]): E = {
      val event = ((Symbol("date") ->> pair._1) :: (Symbol("stories") ->> pair._2) :: HNil).asInstanceOf[labelledGeneric.Repr]
      labelledGeneric.from(event)
    }
  }

  implicit class ProductOps[CC<: Product](caseClazz: CC) {
    def toTupletoMap[Repr <: HList](implicit tupleGeneric: TupleGeneric[CC]): tupleGeneric.Repr = {
      tupleGeneric.to(caseClazz)
    }
  }

}
