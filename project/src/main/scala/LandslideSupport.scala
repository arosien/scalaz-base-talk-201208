package net.rosien.landslide

import sbt._
import Keys._

object LandslideSupport {
  val Landslide = config("landslide")
    
  val settings: Seq[Setting[_]] =
    Seq(
      sourceDirectory in Landslide <<= sourceDirectory(_ / "landslide"),
      watchSources := Seq(file("src/landslide")),
      target in Landslide <<= target(_ / "landslide"),
      includeFilter in Landslide := AllPassFilter
    ) ++ inConfig(Landslide)(Seq(
      mappings <<= (sourceDirectory, target, includeFilter) map LandslideRunner.run
    ))
}

object LandslideRunner {
  def run(input: File, output: File, includeFilter: FileFilter): Seq[(File, String)] = {
    if(!output.exists) output.mkdirs()
    IO.copyDirectory(input, output) 

    Process("landslide --relative --linenos=table %s".format(input.getAbsolutePath), output).!
    
    // return what to copy to target/site: everything except slides.md
    output ** includeFilter --- output --- (input / "slides.md") x relativeTo(output)
  }
}
