package net.rosien.scalaz

import scalaz._
import Scalaz._

object Validation {
  case class Version(
      major: Int, // >= 0 
      minor: Int) // >= 0
  
  object Version {
    def validDigit(digit: Int) = 
      (digit >= 0) ? digit.success[String] | "digit must be >= 0".fail
      
    def validate(major: Int, minor: Int) = 
      (validDigit(major).liftFailNel |@| validDigit(minor).liftFailNel) { Version(_, _) }
  }
        
  case class Dependency(
      organization: String, // non-empty && has namespace 
      artifactId: String,   // non-empty
      version: Version)     // validated
  
  object Dependency {
    def nonEmptyValue(name: String, value: String) = value.isEmpty ? 
        "%s cannot be empty".format(name).fail[String] | value.success
    
    def validOrganizationNamespace(org: String) = org.contains(".") ? 
        org.success[String] | "organization must be some kind of namespace".fail
        
    def validOrganization(org: String) = 
      (nonEmptyValue("organization", org).liftFailNel |@| validOrganizationNamespace(org).liftFailNel) { 
      (_, _) => org 
    }
        
    def validArtifactId(id: String) = nonEmptyValue("artifactId", id)
    
    def validate(organization: String, artifactId: String, version: ValidationNEL[String, Version]) = {
      (validOrganization(organization) |@| validArtifactId(artifactId).liftFailNel |@| version) { 
        Dependency(_, _, _) 
      }
    }
  }
}