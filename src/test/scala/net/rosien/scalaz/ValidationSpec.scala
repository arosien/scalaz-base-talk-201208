package net.rosien.scalaz
import org.specs2.matcher.Matcher
import org.specs2.Specification
import scalaz._
import Scalaz._

class ValidationSpec extends Specification {
  import Validation._
  
  def beSuccessful[E, A]: Matcher[Validation[E, A]] = (v: Validation[E, A]) => (v.fold(_ => false, _ => true), v+" successful", v+" is not successfull")
  def beAFailure[E, A]: Matcher[Validation[E, A]] = (v: Validation[E, A]) => (v.fold(_ => true, _ => false), v+" is a failure", v+" is not a failure")
  def succeedWith[E, A](a: => A) = validationWith[E, A](Success(a))
  def failWith[E, A](e: => E) = validationWith[E, A](Failure(e))
  private def validationWith[E, A](f: => Validation[E, A]): Matcher[Validation[E, A]] = (v: Validation[E, A]) => {
    val expected = f
    (expected == v, v+" is a "+expected, v+" is not a "+expected)
  }
  
  def is = 
    "validate versions" ^
      "digits less then 0 should fail" ! version().invalidDigit ^
      "invalid major and minor has 2 error messages" ! version().invalidVersions ^
      p ^ 
    "validate dependency fields" ^
      "empty values should fail"         ! (Dependency.nonEmptyValue("foo", "") must beAFailure) ^
      "non-eempty values should succeed" ! (Dependency.nonEmptyValue("foo", "bar") must beSuccessful) ^
      "namespaces must contain ."        ! (Dependency.validOrganizationNamespace("foo") must beAFailure) ^
      "namespaces containing . succeed"  ! (Dependency.validOrganizationNamespace("foo.bar") must beSuccessful) ^
      p ^
    end
    
  case class version() {
    def invalidDigit = Version.validDigit(-1) must beAFailure
    def invalidVersions = Version.validate(-1, -2).fail.map(_.len).validation must failWith(2)
  }
}