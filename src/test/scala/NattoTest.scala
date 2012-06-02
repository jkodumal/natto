package natto

import org.specs2.mutable._
  import shapeless._, HList._, Record._

class NattoTest extends Specification {
  
  // Record style

  object firstName extends Field[String] { override def toString = "firstName" }
  object lastName extends Field[String] { override def toString = "lastName" }
  object id extends Field[Long] {override def toString = "id" }

  object badName extends Field[(String, String)]

  type Person = FieldEntry[firstName.type] :: FieldEntry[lastName.type] :: FieldEntry[id.type] :: HNil

  type BadPerson = FieldEntry[badName.type] :: HNil

  val bob : BadPerson = (badName -> ("John", "Doe")) :: HNil
  
  val johnDoeRecord : Person = 
    (firstName -> "John") ::
    (lastName -> "Doe") ::
    (id -> 12L) ::
    HNil




}