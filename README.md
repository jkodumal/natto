natto: Type-safe, typeclass-based conversion from Scala case classes to Soy template data
====

What is natto?
----

natto is an experimental library that provides a type-safe mapping from Scala case classes to `SoyData` (which can be passed to [Soy templates](https://developers.google.com/closure/templates/) for rendering). At their core, most serialization libraries have a magic reflection and/or annotation layer to serialize custom data types without boilerplate. This is nasty, often inflexible, and typically not safe. A few libraries use a typeclass-based approach, which is much more flexible and safe, but at the cost of additional boilerplate. natto attempts to provide the best of these worlds: a completely statically checked system, customizable with implicits, and with minimal boilerplate.

The holy grail would look something like this:

    case class Person(name: String, address: Address)
    case class Address(street: String, city: String)
    
    val johnDoe = Person("John Doe", Address("Elm", "Springfield"))
    
    natto(johnDoe) // yields the following SoyMapData: {name : "John Doe", address: {street: "Elm", city: "Springfield" }}
    
    case class BadPerson(name: String, dob: CustomDate)
    natto(BadPerson("John Doe", CustomDate("8/8/88"))) // fails to compile unless a typeclass instance for CustomDate is provided

The approach taken by natto is to use [shapeless](https://github.com/milessabin/shapeless) `HList`s as an intermediate conversion step. Each case class is first converted to an `HList`, which can then safely be converted to `SoyData` without any additional boilerplate. Some clever type-level programming tricks allow us to convert arbitrary `HList`s (even ones containing nested `HList`s, which originate from nested case classes) to `SoyData` safely. This reduces the boilerplate problem to producing a mapping from a case class to an `HList`. Actually--- an `HList` isn't truly what we want, because we want to preserve the field names of our case classes as map keys for our `SoyData`. This is a bit of a problem, as there's no easy way to access field names in Scala statically. Still, assuming that we figure out some mechanism for generating that mapping cleanly (perhaps via scala macros?), the holy grail above is quite achievable.

Why should I care?
----

Admittedly, there are probably very few projects out there that need to serialize data to Soy. However, the techniques used in natto are fairly universal, and can be applied to many different data transformation problems. `SoyData` is basically JSON, for example--- so natto could very easily be modified to produce JSON data instead of Soy. Other possibilities include an ORM library (think: a safer version of [Salat](https://github.com/novus/salat)).

What's the state of the code?
----

The code is not ready for production. I still do not have a clean solution for generating case class to `HList` mappings automatically, nor have I even begun to think about performance.

That said, take a peek at the tests for the current state of what natto looks like to clients. If you're willing to replace your case classes with shapeless records, then you don't need to wait for a solution to the case class to `HList` mapping problem--- natto already gives you boilerplate-free record to Soy conversion.