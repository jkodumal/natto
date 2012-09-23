package natto.tests

import natto._
import org.specs2.mutable._
import shapeless._, HList._, Record._, TypeOperators._, Functions._, UnaryTCConstraint._

class NattoTest extends Specification {

  type Address = FieldEntry[street.type] :: FieldEntry[city.type] :: HNil

  object street extends Field[String] {override def toString = "street"}
  object city extends Field[String] {override def toString = "city" }

  object firstName extends Field[String] { override def toString = "firstName" }
  object lastName extends Field[String] { override def toString = "lastName" }
  object id extends Field[Long] {override def toString = "id" }
  object address extends Field[Address] {override def toString = "address" }

  type Person = FieldEntry[firstName.type] :: FieldEntry[lastName.type] :: FieldEntry[id.type] :: FieldEntry[address.type] :: HNil

  val johnDoeAddress : Address =
    (street -> "Elm") ::
      (city -> "Springfield") ::
      HNil

  val johnDoeRecord : Person =
    (firstName -> "John") ::
      (lastName -> "Doe") ::
      (id -> 12L) ::
      (address -> johnDoeAddress) ::
      HNil

  // The fact that these compile is witness to their correctness
  "isRecord on HNil" should {
    "compile" in {
      isRecord(HNil : HNil)
    }
  }

  "isRecord on a composite HList" should {
    "compile" in {
      isRecord(johnDoeRecord)
    }
  }

  "The soy data for John Doe" should {
    val soy = nattoMap(johnDoeRecord)
    
    "have a key 'firstName' with value 'John'" in {
      soy.get("firstName").stringValue === "John"
    }

    "have a key 'lastName' with value 'Doe'" in {
      soy.get("lastName").stringValue === "Doe"
    }

    "have a key 'address.street' with value 'Elm'" in {
      soy.get("address.street").stringValue === "Elm"
    }

    "have a key 'address.city' with value 'Springfield'" in {
      soy.get("address.city").stringValue === "Springfield"
    }
  }

  // A pair of arbitrary case classes, one nested
  case class Foo(i : Int, s : String)
  case class Bar(s : String, f: Foo)

  // Publish their `HListIso`'s
  // TODO : Need a way to clean up all this boilerplate mess (maybe macros?)

  // BEGIN BOILERPLATE for iso generation
  object iField extends Field[Int] { override def toString = "i" }
  object sField extends Field[String] {override def toString = "s" }
  object fField extends Field[FooHList] { override def toString = "f" }

  type FooHList = FieldEntry[iField.type] :: FieldEntry[sField.type] :: HNil
  type BarHList = FieldEntry[sField.type] :: FieldEntry[fField.type] :: HNil

  implicit def fooIso = new HListIso[Foo, FooHList](
        ctor = (l: FooHList) => Foo(l.head._2, l.tail.head._2)
      , dtor = (t: Foo) => (iField -> t.i) :: (sField -> t.s) :: HNil 
    )

  implicit def barIso(implicit fi: HListIso[Foo, FooHList]) = new HListIso[Bar, BarHList](
        ctor = (l: BarHList) => Bar(l.head._2, fi.fromHList(l.tail.head._2))
      , dtor = (t: Bar) => (sField -> t.s) :: (fField -> fi.toHList(t.f)) :: HNil
    )
  // END BOILERPLATE for iso generation

  val foo = Foo(1, "foo")
  val bar = Bar("bar", foo)

  "The soy data for bar" should {
    val soy = nattoMap(makeIso[Bar, BarHList](bar))

    "have a key 's' with value 'bar'" in {
      soy.get("s").stringValue === "bar"
    }

    "have a key 'f.s' with value 'foo'" in {
      soy.get("f.s").stringValue === "foo"
    }
  }

}