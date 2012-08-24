# scalaz "For the Rest of Us"

<div style="border-radius: 10px; background: #EEEEEE; padding: 20px; text-align: center; font-size: 1.5em">
  Adam Rosien <br/>
  <code>arosien@box.com | adam@rosien.net</code> <br/>
  <br/>
  <code>@arosien #scalasv #scalaz</code>
</div>

![](img/box.png)

---

This talk is *not* about:

 * Monads
 * Applicative Functors
 * Category theory
 * Other really cool stuff

---

.notes: This is probably not the best way to describe the purpose of the talk.

    !scala
    val talkTopics = for {
      situation <- everyDaySituations
      when      <- scalaz(situation)
      how       <- when(situation)
    } yield when |+| how

---

In `build.sbt`:

    !scala
    libraryDependencies += 
      "org.scalaz" %% "scalaz" % "6.0.4"

Then:

    !scala
    import scalaz._
    import Scalaz._

    // profit

.notes: This is _scalaz 6._ Also, assume this is imported in all code snippets.

--- 

# Memoization

--- 

# Memoization

    !scala
    def expensive(foo: Foo): Bar = ...

    val f: Foo

    expensive(f) // $$$
    expensive(f) // $$$
    expensive(f) // $$$
    ...          

--- 

# Memoization

    !scala
    def expensive(foo: Foo): Bar = ...

    val memo = immutableHashMapMemo { 
      foo: Foo => expensive(foo) 
    }

    val f: Foo

    memo(f) // $$$ (cache miss & fill)
    memo(f) // 1¢  (cache hit)
    memo(f) // 1¢  (cache hit)
    ...     

---

# Memoization

Many memoization strategies:

    !scala
    immutableHashMapMemo[K, V]
    
    mutableHashMapMemo[K, V]

    arrayMemo[V](size: Int) // fixed size

    // make your own! :/
    memo[K, V](f: (K => V) => K => V) 

.notes: Super-nerdy: the memoizing strategies are just functions of `K => V`, which means the generic `memo()` constructor has the same signature as the Y-combinator!

---

# Style

    !scala
    val a: A
    val f: A => B
    val g: B => C

    g(f(a))     // composition, vs....

    a |> f |> g // "unix-pipey"

When you want to emphasize the pipeline nature 
of nested functions: `g(f)` to `f |> g`

---

# Style

    !scala
    val p: Boolean

    // ternary-operator-ish
    p ? "yes" | "no" // if (p) "yes" else "no" 

    val o: Option[String]
    
    o | "meh"        // o.getOrElse("meh") 

.notes: When you just can't stand typing `if` and `else` all the time.  It's so... _imperative_.

---

# Syntax Helpers 

    !scala
    Some("foo")  // Some[String]
    "foo".some   // Option[String]

    None         // None.type
    none         // Option[Nothing], oops!
    none[String] // Option[String]

---

# Syntax Helpers

    !scala
    Right(42) // Right[Nothing, Int], oops!
    Right[String, Int](42) // verbose
    42.right[String] // Either[String, Int]

    Left("meh") // Left[String, Nothing], oops!
    Left[String, Int]("meh") // verbose
    "meh".left[Int] // Either[String, Int]
---

# Type-safety

---

# Type-safety

Type-safe equality!

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

---

# Validation

The Java way, eww:

    !scala
    def fetch(uri: URI): String

    def meh(t: Throwable) = ...
    def gotIt(s: String)  = ...

    try {
      val result = fetch(...)
      gotIt(result)
    } catch {
      case e: meh(e)
    }

---

# Validation

In Scala we represent the two cases in one type, `Either`:

    !scala
    def fetch(uri: URI): Either[Throwable, String]

    def meh(t: Throwable) = ...
    def gotIt(s: String)  = ...

    val result = fetch(...) 

---

# Validation

And handle it in multiple ways. Via pattern match:

    !scala
    result match {
      case Left(l)  => 
        // Left is a fail, right?
        meh(l)
      case Right(r) => 
        // Right must be right?
        gotIt(r)
    }

---

# Validation

Via `fold()`:

    !scala
    result.fold(
      l => meh(l),
      r => gotIt(r))

---

# Validation

Via for-comprehension:

    !scala
    for {
      
    }

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
          FooNode(Foo("child2", 2)))) // <-- * 4

Task: Create a new tree where the *second child's* `factor` is multiplied by 4.

---

# Lenses

Let's try all at once:

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

Eww.

---

# Lenses

    !scala
    Lens[Thing, View](
      get: Thing => View,
      set: (Thing, View) => Thing)

    val thing: Thing
    val lens:  Lens[Thing, View] = ...
    
    val view: View = lens(thing) // apply = get

    // "set" a transformed View
    val thing2: Thing = lens.mod(thing, v: View => ...)

    // Lots of other operations on a Lens...

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

# Writer/Logger


---

# Thanks
