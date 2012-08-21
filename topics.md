_Note: this is scalaz 6._

```scala
import scalaz._
import Scalaz._
// profit
```

syntax helpers: 

```scala
val a: A
val f: A => A
val g: A => B

val b: B = a |> f |> g // g(f(a))

val p: Boolean
val isP: String = p ? "yes" | "no" // if (p) "yes" else "no"

val o: Option[String]
val s: String = o | "meh" // o.getOrElse("meh")
```

covariance helpers: 
```scala
.none, .some
.left, .right
```

type-safety:
```scala
=== via Equal[A]
```

destructuring tai-chi with `fold`:
 * pattern-matching allows deconstruction of a value in arbitrary ways, `fold` only in the two allowed ways

```scala
val mojo: Option[String] = "whiskey".some
val grassHopper: Boolean = mojo.fold(some => true, none => false) // true

// Either.fold() already defined in Scala
```

Validation vs. Either
 * better names: Failure/Success vs. Left/Right
 * monadic without the need for left/right projection, defaults to right
 * accumulates errors via Semigroup append |+|
 * ValidationNEL[X, A] alias for Validation[NonEmptyList[X], A]
 * NonEmptyList has a Semigroup so you get error accumulation "for free"
 * multi-level case class validation
   * companion object apply() pattern
 * Validation is Applicative so you can combine multiple Validations where failures are accumulated

Lenses
 * removing case class copy-cruft

Dependency Injection
 * Reader monad, really just function composition
 * Why this doesn't work without scalaz: no map/flatMap for Function1

memoization:
```scala
def expensive(foo: Foo): Bar = ...
val memo = immutableHashMapMemo { foo: Foo => expensive(foo) }

val f: Foo
val b: Bar = memo(f) // expensive(f) is cached
```

Writer/Logger

"thinking in types"

composition
 * fp folks always talk about composition. what's the big deal? 
 * examples of non-composable
 * what you get "for free" with composition

monads: trivial sequence -> for-comprehension -> add monads, and hence semantics, without changing the program
 * "a monadic function is just a pure function from input values to output computations"
