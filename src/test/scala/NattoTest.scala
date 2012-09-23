package natto.tests

import natto._
import org.specs2.mutable._
import shapeless._, HList._, Record._, TypeOperators._, Functions._, UnaryTCConstraint._

class NattoTest extends Specification {

  // A pair of arbitrary case classes, one nested
  case class Foo(i : Int, s : String)
  case class Bar(s : String, f: Foo)
  type FooHList = Int :: String :: HNil
  type BarHList = String :: FooHList :: HNil

  // Publish their `HListIso`'s
  implicit def fooIso = HListIso(Foo.apply _, Foo.unapply _)
  implicit def barIso = HListIso(Bar.apply _, Bar.unapply _)


  val foo = Foo(1, "foo")
  val bar = Bar("bar", foo)

  // Yields a FooHList
  makeIso(foo)

  // Yields a BarHList
  makeIso[Bar, BarHList](bar) // you have to help out scala's type inference in this case

  type Address = FieldEntry[street.type] :: FieldEntry[city.type] :: HNil

  object street extends Field[String] {override def toString = "street"}
  object city extends Field[String] {override def toString = "city" }

  object firstName extends Field[String] { override def toString = "firstName" }
  object lastName extends Field[String] { override def toString = "lastName" }
  object id extends Field[Long] {override def toString = "id" }
  object address extends Field[Address] {override def toString = "address" }

  type Person = FieldEntry[firstName.type] :: FieldEntry[lastName.type] :: FieldEntry[id.type] :: FieldEntry[address.type] :: HNil


  val johnDoeAddress : Address =
    (street -> "elm") ::
      (city -> "springfield") ::
      HNil

  val johnDoeRecord : Person =
    (firstName -> "John") ::
      (lastName -> "Doe") ::
      (id -> 12L) ::
      (address -> johnDoeAddress) ::
      HNil

  isRecord(HNil: HNil)

  isRecord(johnDoeRecord)
  isRecord(johnDoeAddress)

  nattoMap(johnDoeRecord)
  nattoMap(johnDoeAddress)


}