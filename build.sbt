import net.rosien.landslide._

name := "scalaz-base-talk"

organization := "net.rosien"

site.settings

LandslideSupport.settings ++ Seq(site.addMappingsToSiteDir(mappings in LandslideSupport.Landslide, ""))

ghpages.settings

git.remoteRepo := "git@github.com:arosien/scalaz-base-talk.git"
