package net.rosien.scalaz

import scalaz._
import Scalaz._

object Memo {

  case class Foo()
  case class Bar()

  def expensive(foo: Foo): Bar = {
    println("expensive: %s".format(foo))
    Bar()
  }

  def cache = collection.mutable.Map[Foo, Bar]()

  def memo = immutableHashMapMemo { 
    foo: Foo => expensive(foo) 
  }
}
