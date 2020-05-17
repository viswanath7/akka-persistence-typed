package com.example.generic

import shapeless._
import ops.hlist.Tupler

trait TupleGeneric[CC <: Product] extends Serializable {
  type Repr <: Product
  def to(caseClass : CC) : Repr
  def from(representation : Repr) : CC
}

object TupleGeneric {
  type Aux[C <: Product, R] = TupleGeneric[C] { type Repr = R }
  def apply[C <: Product](implicit tupleGeneric: TupleGeneric[C]): Aux[C, tupleGeneric.Repr] = tupleGeneric

  implicit def mkTG[C <: Product, L <: HList, R <: Product]
  (implicit caseClassGeneric: Generic.Aux[C, L],
   tupler: Tupler.Aux[L, R],
   tupleGeneric: Generic.Aux[R, L]): Aux[C, R] =
    new TupleGeneric[C] {
      type Repr = R
      def to(caseClass : C) : Repr = caseClassGeneric.to(caseClass).tupled
      def from(representation : Repr) : C = caseClassGeneric.from(tupleGeneric.to(representation))
    }
}