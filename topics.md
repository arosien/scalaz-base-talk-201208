shout out to the scalaz authors!

General topics:

 * syntax helpers
 * covariance helpers
 * type-safety

Bigger:

 * Validation vs. Either: lots better
 * Lenses: removing case class copy-cruft; "deep" pointers and "updates"
 * memoization
 * Writer/Logger
 * Dependency Injection: Reader monad, really just function composition; why this doesn't work without scalaz: no map/flatMap for Function1

"thinking in types"
 * _screenshot of eclipse type inference_
 * _one-to-one principle_: one type used per function signature
 * Rather than thinking about how to jam our ideas into the trappings of the language--class hierarchies and such--we "merely" work with the things as types, transforming them with commonly known functions like `map`, `fold`, and so on, then adding context and effects to them in a few well-known ways (dependency-injection = `Reader`, accumulate logging information = `Writer`/`Logger`, perform a transformation using the current state and produce a new state = `State`, etc.).

composition
 * fp folks always talk about composition. what's the big deal? 
 * examples of non-composable
 * what you get "for free" with composition

monads: trivial sequence -> for-comprehension -> add monads, and hence semantics, without changing the program
 * "a monadic function is just a pure function from input values to output computations"
