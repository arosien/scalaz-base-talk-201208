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

![](img/ma.jpg)

`scalaz` has a (undeserved?) reputation as being, well, kind of crazy.

So this talk is specifically *not* about:

 * Functors, Monads, or Applicative Functors
 * Category theory
 * Other really cool stuff you should learn about (eventually)

---

This talk *is* about **every-day situations** where `scalaz` can:

 * *Reduce* syntactical noise
 * Provide useful types that solve *many classes* of problems
 * *Add* type-safety with minimal "extra work"

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

This isn't good:

    !scala
    case class SSN(
      first3: Int, 
      second2: Int, 
      third4: Int)

    SSN(123, 123, 1234) 
    //       ^^^ nooooo!

---

# Validation

This shouldn't be possible:

    !scala
    case class Version(major: Int, minor: Int)

    Version(1, -1) 
    //         ^^ nooooo!

---

# Validation

Meh:

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

The problem is that the types as-is aren't really accurate.  
`String`s and `Int`s are being used too broadly.
We really want "`Int`s greater than zero", 
"`String`s that match a pattern", etc.

You can do the checks in the constructor:

    !scala
    case class Version(major: Int, minor: Int) {
      require(
        major >= 0, 
        "major must be >= 0: %s".format(major))
      require(
        minor >= 0, 
        "minor must be >= 0: %s".format(minor))
    }

---

# Validation

But this has downsides:

 * Validation failures happen as late as possible.
 * You only get one failure, but more than one violation may be happening.
 * You have to catch exceptions, which is just tedious.

---

# Validation

    !scala
    val major: Int = ...
    val minor: Int = ...
    val version: ??? =
      Version.validate(major, minor)

    version | Version(1, 0) // provide default

    // handle failure and success
    version.fold(
      fail: ???        => ...,
      success: Version => ...)

---

# Validation

Using `scalaz`, a `Validation` can either be a `Success` or `Failure`:

    !scala
    Version.validate(1, 2)
    // Success(Version(1, 2))

    Version.validate(1, -1)
    // Failure(NonEmptyList("digit must be >= 0"))

---

# Validation

Model the `>= 0` constraint:

    !scala
    case class Version(
      major: Int, // >= 0
      minor: Int) // >= 0

    object Version {
      def validDigit(digit: Int):
        Validation[String, Int] = (digit >= 0) ? 
          digit.success[String] | 
          "digit must be >= 0".fail
        
      ...
    }

---

# Validation

Combine constraints:

    !scala
    object Version {
      def validDigit(digit: Int): 
        Validation[String, Int] = ...
        
      def validate(major: Int, minor: Int) = 
        (validDigit(major).liftFailNel |@| // huh?
         validDigit(minor).liftFailNel) {  // huh?
          Version(_, _)                    // huh?
        }
    }

---

# Validation

`validDigit(major).liftFailNel`... wtf!?

    !scala
    validDigit(major)
    // Validation[String, Version] 

    validDigit(major).liftFailNel
    // Validation[NonEmptyList[String], Version] 

    // (already defined in scalaz)
    type ValidationNEL[X, A] =
      Validation[NonEmptyList[X], A]

    validDigit(major).liftFailNel
    // ValidationNEL[String, Version]

---

# Validation

    !scala
    val maj = validDigit(major).liftFailNel
    val min = validDigit(minor).liftFailNel
    // Both ValidationNEL[String, Int]
    //                            ^^^

    val mkVersion = Version(_, _)
    // (Int, Int) => Version

    val version = (maj |@| min) { mkVersion }
    // ValidationNEL[String, Version]
    //                       ^^^^^^^

---

# Validation

The general form of combining `ValidationNEL`:

    !scala
    (ValidationNEL[X, A] |@|
     ValidationNEL[X, B]) {
      (A, B) => C
    } // ValidationNEL[X, C]

    (ValidationNEL[X, A] |@|
     ValidationNEL[X, B] |@|
     ValidationNEL[X, C]) {
      (A, B, C) => D
    } // ValidationNEL[X, D]

    // etc.

---

# Validation

The "rules":

    !scala
    Success |@| Success // Success
    Success |@| Failure // Failure
    Failure |@| Success // Failure
    Failure |@| Failure // Failure 
    // and accumulate fail values!

    // Accumulate?
    NonEmptyList("foo") |+| NonEmptyList("bar")
    // NonEmptyList("foo", "bar")

    // |+|? "appends" things according to rules

---

# Validation

An improvement?

 * `Validation`/`Success`/`Failure` vs. `try`/`catch` or `Either`/`Left`/`Right`.
 * Each rule is just a function producing a `Validation`.
 * Rules can be composed together into new validations, of differing types.
 * Composed rules accumulate all the errors along the way.

![yes](img/yes.jpg)

---

# Lenses

---

# Lenses

Let's say you have some nested structure like a tree:

    !scala
    // the data
    case class Foo(name: String, factor: Int)

    // a node of the tree
    case class FooNode(
      value: Foo, 
      children: Seq[FooNode] = Seq())

---

# Lenses

Make a tree of `Foo`'s:

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

Thank the `scalaz` authors: runarorama, retronym, tmorris and lots others.

Credits, sources and references:

 * This presentation: [https://github.com/arosien/scalaz-base-talk-201208](https://github.com/arosien/scalaz-base-talk-201208)
 * Yuvi Masory, [Scalaz, Monads, Functors and You](http://yuvimasory.com/talks)
 * [scalaz homepage](http://code.google.com/p/scalaz/), [scalaz 6.0.4 source cross-reference](http://scalaz.github.com/scalaz/scalaz-2.9.1-6.0.4/doc.sxr/index.html)
 * [jrwest/learn_you_a_scalaz](https://github.com/jrwest/learn_you_a_scalaz)
 * [debasishg/tryscalaz](https://github.com/debasishg/tryscalaz)
 * Runar Oli, [Dead-Simple Dependency Injection](http://lanyrd.com/2012/nescala/sqygc)
 * Tony Morris, [Dependency Injection Without the Gymnastics](http://phillyemergingtech.com/2012/system/presentations/di-without-the-gymnastics.pdf)


