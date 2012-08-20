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

Writer/Logger

composition
 * fp folks always talk about composition. what's the big deal? 
 * examples of non-composable
 * what you get "for free" with composition

monads: trivial sequence -> for-comprehension -> add monads, and hence semantics, without changing the program

