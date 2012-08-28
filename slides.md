<div style="border-radius: 10px; background: #EEEEEE; padding: 20px; text-align: center; font-size: 1.5em">
  <big><b>scalaz "For the Rest of Us"</b></big> </br>
  </br>
  Adam Rosien <br/>
  <code>arosien@box.com && adam@rosien.net</code> <br/>
  <br/>
  <code>@arosien #scalasv #scalaz</code>
</div>

![Box](img/box.png)

---

![](img/wonka-npe.jpeg)

.notes: Scala is great.

---

![](img/ma.jpg)

.notes: `scalaz` has a (undeserved?) reputation as being, well, kind of crazy.

---

But `scalaz` is *AWESOME*. 

---

This talk is specifically *not* about:

 * Monads
 * Applicative Functors
 * Category theory
 * Other really cool stuff you should learn about (eventually)

.notes: Thank your local `scalaz` authors: runarorama, retronum, tmorris and lots others.

---

This talk *is* about every-day situations where `scalaz` can:

 * Reduce syntactical noise
 * Add type-safety with minimal "extra work"
 * Provide useful types that solve many classes of problems

.notes: We'll talk about `scalaz` for (1) memoization, (2) domain model validation, (3) dependency injection and (4) better style.

---

# Getting Started 

In `build.sbt`:

    !scala
    libraryDependencies += 
      "org.scalaz" %% "scalaz-core" % "6.0.4"

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

The goal: cache the result of an expensive computation.

    !scala
    def expensive(foo: Foo): Bar = ...

    val f: Foo

    expensive(f) // $$$
    expensive(f) // $$$
    ...          

.notes: Assumption: `expensive` produces the same output for every input, i.e., is referentially-transparent.

--- 

# Memoization

Typically you might use a `mutable.Map` to cache results:

    !scala
    val cache = collection.mutable.Map[Foo, Bar]()

    cache.getOrElseUpdate(f, expensive(f)) // $$$
    cache.getOrElseUpdate(f, expensive(f)) // 1¢

.notes: Downsides: the cache is not the same type as the function: `Foo => Bar` vs. `Map[Foo, Bar]`. It's also not DRY.

--- 

# Memoization

You can try to make it look like a regular function, avoiding the `getOrElseUpdate()` call:

    !scala
    val cache: Foo => Bar = 
      collection.mutable.Map[Foo, Bar]()
        .withDefault(expensive _)

    cache(f) // $$$ (miss & NO fill)
    cache(f) // $$$ (miss & NO fill)

But it doesn't actually cache.

--- 

# Memoization

In `scalaz`:

    !scala
    def expensive(foo: Foo): Bar = ...

    // Memo[Foo, Bar]
    val memo = immutableHashMapMemo { 
      foo: Foo => expensive(foo) 
    }

    val f: Foo

    memo(f) // $$$ (cache miss & fill)
    memo(f) // 1¢  (cache hit)

---

# Memoization

Many memoization strategies:

    !scala
    immutableHashMapMemo[K, V]
    
    mutableHashMapMemo[K, V]

    // remove + gc unreferenced entries
    weakHashMapMemo[K, V]   

    // fixed size, K = Int
    arrayMemo[V](size: Int) 

.notes: Super-nerdy: the memoizing strategies are just functions of `K => V`, which means the generic `memo()` constructor has the same signature as the Y-combinator!

---

# Style

---

# Style

Remove the need for temporary variables:

    !scala
    val f: A => B
    val g: B => C

    // using temps:
    val a: A = ...
    val b = f(a)
    val c = g(b)

    // or via composition, which is a bit ugly:
    val c = g(f(a))     

    // "unix-pipey"!
    val c = a |> f |> g 

---

# Style

When you just can't stand all that (keyboard) typing: 

    !scala
    val p: Boolean

    // ternary-operator-ish
    p ? "yes" | "no" // if (p) "yes" else "no" 

    val o: Option[String]
    
    o | "meh"        // o.getOrElse("meh") 

---

# Style

More legible (and more type-safe):

    !scala
    // scala
    Some("foo")  // Some[String]
    None         // None.type

    // scalaz
    "foo".some   // Option[String]
    none         // Option[Nothing], oops!
    none[String] // Option[String]

---

# Style

More legible (and more type-safe):

    !scala
    // scala
    Right(42)   // Right[Nothing, Int], oops!
    Left("meh") // Left[String, Nothing], oops!
    Right[String, Int](42)   // verbose
    Left[String, Int]("meh") // verbose

    // scalaz
    42.right[String] // Either[String, Int]
    "meh".left[Int]  // Either[String, Int]

---

# Validation

---

# Validation

These shouldn't be possible:

    !scala
    case class SSN(
      first3: Int, 
      second2: Int, 
      third4: Int)

    SSN(123, 123, 1234) 
    //       ^^^ nooooo!

---

# Validation

These shouldn't be possible:

    !scala
    case class Version(major: Int, minor: Int)

    Version(1, -1) 
    //         ^^ nooooo!

---

# Validation

These shouldn't be possible:

    !scala
    case class Dependency(
      organization: String,
      artifactId: String,
      version: Version)

    Dependency("zerb", "", Version(1, 2))
    //                 ^^ nooo!

---

# Validation

![noooooo](img/no.jpg)

---

# Validation

TODO: JUST A LIST OF TOPICS, WILL BE DELETED

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

---

# Lenses

TODO: WHEN? WHY?

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

TODO: these are just notes

 * Reader monad, really just function composition
 * Why this doesn't work without scalaz: no map/flatMap for Function1

---

# Thanks!

<center>
<big><b>scalaz "For the Rest of Us"</b></big> </br>
  </br>
  Adam Rosien <br/>
  <code>arosien@box.com && adam@rosien.net</code> <br/>
  <br/>
  <code>@arosien #scalasv #scalaz</code>
</center>

Credits, sources and references:

 * [scalaz homepage](http://code.google.com/p/scalaz/), [scalaz 6.0.4 source cross-reference](http://scalaz.github.com/scalaz/scalaz-2.9.1-6.0.4/doc.sxr/index.html)
 * [jrwest/learn_you_a_scalaz](https://github.com/jrwest/learn_you_a_scalaz)
 * [debasishg/tryscalaz](https://github.com/debasishg/tryscalaz)
 * Runar Oli, [Dead-Simple Dependency Injection](http://lanyrd.com/2012/nescala/sqygc)
 * Tony Morris, [Dependency Injection Without the Gymnastics](http://phillyemergingtech.com/2012/system/presentations/di-without-the-gymnastics.pdf)


