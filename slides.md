# scalaz "For the Rest of Us"

<div style="border-radius: 10px; background: #EEEEEE; padding: 20px; text-align: center; font-size: 1.5em">
  Adam Rosien <br/>
  Box <br/>
  <code>adam@rosien.net</code> <br/>
  <br/>
  <code>@arosien</code><br/>
  <code>#scalasv #scalaz</code>
</div>

---

This talk is *not* about:

 * Monads
 * Applicative Functors
 * Category theory
 * Other really cool stuff

---

Note: this is _scalaz 6._ 

Also, assume this is imported in all slide code snippets.

    !scala

    import scalaz._
    import Scalaz._

    // profit

---

# Syntax Helpers

    !scala
    val a: A
    val f: A => B
    val g: B => C

    g(f(a))     // composition

    a |> f |> g // "unix-pipey"

---

# Syntax Helpers

    !scala
    val p: Boolean

    if (p) "yes" else "no" // vs.
    p ? "yes" | "no"       // "ternary-ish"


    val o: Option[String]

    o.getOrElse("meh") // vs.
    o | "meh" 

---

# Syntax Helpers 

Sometimes you're lazy and want the compiler to infer the correct type 
so you don't have to declare it yourself. But be careful, not all
constructors return the "proper" type, so you may be constraining
yourself to too-specific a type, especially if consumers have type
parameters that are not covariant.

    !scala
    Some("foo") // Some[String], oops
    "foo".some  // Option[String]


    Right("foo") // Right[Nothing, String]
    Right[Throwable, String]("foo") // meh
    "foo".right[Throwable] 
    // Right[Throwable, String]

---

# Type-safe equality

    !scala
    scala> "foo" == 1
    res16: Boolean = false

    scala> "foo" === 1
    <console>:29: error: type mismatch;
     found   : Int(1)
     required: java.lang.String
           "foo" === 1
                     ^

    scala> "foo" === "bar"
    res18: Boolean = false

---

# Validation

 * better names: Failure/Success vs. Left/Right
 * monadic without the need for left/right projection, defaults to right
 * accumulates errors via Semigroup append |+|
 * ValidationNEL[X, A] alias for Validation[NonEmptyList[X], A]
 * NonEmptyList has a Semigroup so you get error accumulation "for free"
 * multi-level case class validation
   * companion object apply() pattern
 * Validation is Applicative so you can combine multiple Validations where failures are accumulated

---

# Lenses

    !scala
    case class Foo(name: String, factor: Int)

    case class FooNode(
      value: Foo, 
      children: Seq[FooNode] = Seq())

---

# Lenses

    !scala
    val tree = 
      FooNode(
        Foo("root", 11),
        Seq(
          FooNode(Foo("child1", 1)),
          FooNode(Foo("child2", 2))))

Task: Create a new tree where the second child's factor is multiplied by 4.

---

# Lenses

    !scala
    val secondTimes4: FooNode => FooNode = 
      node => node.copy(children = {
        val second = node.children(1)
        node.children.updated(
          1, 
          second.copy(
            value = second.value.copy(
              factor = second.value.factor * 4)))
      })

---

# Lenses

    !scala
    val second: Lens[FooNode, FooNode] = 
      Lens(
        _.children(1),
        (node, c2) => node.copy(
          children = node.children.updated(1, c2)))

    val value: Lens[FooNode, Foo] = 
      Lens(
        _.value,
        (node, value) => node.copy(value = value))

    val factor: Lens[Foo, Int] = 
      Lens(
        _.factor, 
        (foo, fac) => foo.copy(factor = fac))

---

# Lenses

Lenses _compose_:

    !scala
    val secondFactor = 
      second andThen value andThen factor

---

# Lenses

    !scala
    /* FooNode(
         Foo("root", 11),
         Seq(
           FooNode(Foo("child1", 1)),
           FooNode(Foo("child2", 2))))
                                 ^
                                 ^
                                 ^  */
    secondFactor(tree)        // 2 

---

# Lenses

    !scala
    /* FooNode(
         Foo("root", 11),
         Seq(
           FooNode(Foo("child1", 1)),
           FooNode(Foo("child2", 2))))
                                 ^
                                 ^  */
    secondFactor.mod(tree,   _ * 4)
    /* FooNode(                  ^
         Foo("root", 11),        ^
         Seq(                    ^
           FooNode(Foo("child1", ^)),
           FooNode(Foo("child2", 8))))
     */
     

---

# Dependency Injection

 * Reader monad, really just function composition
 * Why this doesn't work without scalaz: no map/flatMap for Function1

--- 

# Memoization

    !scala
    def expensive(foo: Foo): Bar = ...

    // a read-through cache
    val memo = immutableHashMapMemo { 
      foo: Foo => expensive(foo) 
    }

    val f: Foo
    val b: Bar = memo(f) // cache miss & fill

    memo(f) // cache hit
    memo(f) // cache hit
    ...
---

# Writer/Logger


---

# Thanks
