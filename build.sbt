import net.rosien.landslide._

name := "scalaz-base-talk-201208"

organization := "net.rosien"

version := "1.0-SNAPSHOT"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "6.0.4"

site.settings

LandslideSupport.settings ++ Seq(site.addMappingsToSiteDir(mappings in LandslideSupport.Landslide, ""))

ghpages.settings

git.remoteRepo := "git@github.com:arosien/scalaz-base-talk-201208.git"

initialCommands := """
import scalaz._
import Scalaz._
"""

