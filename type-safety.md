# Type-safety

.notes: This has nothing to do with scalaz.

    !scala
    trait Train
    trait Plane
    trait Automobile
    trait Itinerary

    def travel(
      trainId: Long, 
      planeId: Long,
      automId: Long): Itinerary

---

# Type-safety

Was it...

    !scala
    travel(trainId, automId, planeId)

    travel(trainId, planeId, automId)

    travel(planeId, automId, trainId)

...?

---

# Type-safety

What if you accidentally typed:

    !scala
    travel(trainId, automId, automId)

.notes: "typed" har har har.

---

# Type-safety

    !scala
    case class Id[+A](value: Long)

    def travel(
      trainId: Id[Train], 
      planeId: Id[Plane],
      automId: Id[Automobile]): Itinerary

.notes: You can then add the implicit conversion `implicit def idToLong[A](id: Id[A]) = id.value` so you don't have to write `id.value` all the time.

---

# Type-safety

    !scala
    travel(trainId, automId, planeId) // BOOM!

    travel(trainId, planeId, automId) // compiles

    travel(planeId, automId, trainId) // BOOM!

    travel(trainId, automId, automId) // BOOM!

---


