package net.rosien.scalaz

import scalaz._
import Scalaz._

object DependencyInjection {
  trait SystemProperties {
    def get(key: String): Option[String]
  }

  object SystemProperties {
    def apply(m: Map[String, String]): SystemProperties = new SystemProperties {
      def get(key: String): Option[String] = m.get(key)
    }

    def default: SystemProperties = SystemProperties(sys.props.toMap)
  }
  
  case class Foo(name: String)
  case class Bar(count: Int)
  case class Baz(foo: Foo, bar: Bar)

  def mkFoo(props: SystemProperties): Option[Foo] = for {
    name <- props.get("foo.name")
  } yield Foo(name)
  
  def mkBar(props: SystemProperties): Option[Bar] = for {
    count <- props.get("bar.count")
  } yield Bar(count.toInt)
  
  def mkBaz(props: SystemProperties): Option[Baz] = for {
    foo <- mkFoo(props)
    bar <- mkBar(props)
  } yield Baz(foo, bar)
  
  val doBaz: Baz => Unit = baz => println(baz)
  
  def doStuff0 = {
    val props = SystemProperties.default
    for {
      baz <- mkBaz(props)
    } yield doBaz(baz)
  }
  
  // val -> method parameter
  def doStuff1(props: SystemProperties) = for {
    baz <- mkBaz(props)
  } yield doBaz(baz)
  
  // curry parameter
  val doStuff2: SystemProperties => Unit = props => for {
    baz <- mkBaz(props)
  } yield doBaz(baz)
  
  val monady1 = for {
    props <- SystemProperties.default.pure[Identity]
    baz <- mkBaz(props)
  } yield doBaz(baz)
  
  val monady2 = for {
    props <- ask[Identity, SystemProperties]
    baz <- mkBaz(props)
  } yield doBaz(baz)
  
  
}
