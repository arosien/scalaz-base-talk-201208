Shout out to the scalaz authors!

Specific types for every-day problems:
 * memoization: `Memo`
 * just better: `Validation` for domain validation
 * "deep" pointers and "updates": `Lens` (removing case class copy-cruft)
 * dependency injection: `Reader` (really just function composition; why this doesn't work without scalaz: no map/flatMap for Function1)
 * decouple logging for flexibility: `Logger` aka `Writer`

General stuff:
 * syntax helpers
 * covariance helpers
 * type-safety

Meta: "thinking in types"
 * _screenshot of eclipse type inference_
 * _one-to-one principle_: one type used per function signature
 * Rather than thinking about how to jam our ideas into the trappings of the language--class hierarchies and such--we "merely" work with the things as types, transforming them with commonly known functions like `map`, `fold`, and so on, then adding context and effects to them in a few well-known ways (dependency-injection = `Reader`, accumulate logging information = `Writer`/`Logger`, perform a transformation using the current state and produce a new state = `State`, etc.).
 * tension between not explicitly typing, because the compiler can properly infer, and being able to inspect the type, via the editor via the compiler
 * adding more types potentially creates N more adapters to that type, so you want to minimize the amount of new methods signatures you will maybe need to adapt, so having a set of reusable (semantically and structurally well-known) methods reduces the cost of new classes, otherwise we'd just be using lists everwhere

Meta: composition
 * fp folks always talk about composition. what's the big deal? 
 * examples of non-composable
 * what you get "for free" with composition

monads: trivial sequence -> for-comprehension -> add monads, and hence semantics, without changing the program
 * "a monadic function is just a pure function from input values to output computations"
