import com.typesafe.sbt.SbtNativePackager.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys.{javaOptions, javacOptions, scalacOptions}
import sbt._

//settings for all the projects
lazy val commonSettings = Seq(

	organization := "comp.bio.aging",

	scalaVersion :=  "2.12.1",

  crossScalaVersions := Seq("2.12.1", "2.11.8"),

	version := "0.0.1",

	unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value,

	updateOptions := updateOptions.value.withCachedResolution(true), //to speed up dependency resolution

	resolvers += sbt.Resolver.bintrayRepo("comp-bio-aging", "main"),

	resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases"),

	maintainer := "Anton Kulaga <antonkulaga@gmail.com>",

	packageDescription := """benchling-client""",

	bintrayRepository := "main",

	bintrayOrganization := Some("comp-bio-aging"),

	licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0")),

	isSnapshot := true,

	exportJars := true,

	scalacOptions ++= Seq( "-target:jvm-1.8", "-feature", "-language:_" ),

	javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint", "-J-Xss5M", "-encoding", "UTF-8")
)

commonSettings

lazy val circeVersion = "0.7.0"

lazy val  benchlingClient = crossProject
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(commonSettings: _*)
  .settings(
    mainClass in Compile := Some("comp.bio.aging.Benchling"),

    fork in run := true,

    parallelExecution in Test := false,

    packageSummary := "benchlingClient",

    name := "benchling-client",

		libraryDependencies ++= Seq(
			"fr.hmil" %%% "roshttp" % "2.0.1",
			"com.github.melrief" %% "purecsv" % "0.0.9"
		),
		libraryDependencies ++= Seq(
			"io.circe" %%% "circe-core",
			"io.circe" %%% "circe-generic",
			"io.circe" %%% "circe-parser"
		).map(_ % circeVersion)
	)
  .jvmSettings(
    libraryDependencies ++= Seq(
			"org.jdom" % "jdom" % "1.1.3",
      "org.sbolstandard" % "libSBOLj" % "2.1.1" exclude("jdom", "jdom"),
			"net.ruippeixotog" %% "scala-scraper" % "1.2.0",
			"com.github.pathikrit" %% "better-files" % "2.17.1",
			"com.lihaoyi" % "ammonite" % "0.8.2" % Test cross CrossVersion.full
    ),
		initialCommands in (Test, console) := """ammonite.Main().run()"""
  )
  .jsSettings(
    jsDependencies += RuntimeDOM % Test
  )

lazy val benchlingClientJVM = benchlingClient.jvm

lazy val benchlingClientJS = benchlingClient.js