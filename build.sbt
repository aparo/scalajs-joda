lazy val root = project.in(file(".")).
  enablePlugins(ScalaJSPlugin)

name := "joda Datetime facade for Scala.js"

normalizedName := "joda-scalajs"

version := "0.1"

organization := "io.megl"

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.10.4", "2.11.6")

homepage := Some(url("http://www.querki.net/"))

licenses += ("MIT License", url("http://www.opensource.org/licenses/mit-license.php"))

scmInfo := Some(ScmInfo(
    url("https://github.com/aparo/scalajs-joda"),
    "scm:git:git@github.com/aparo/scalajs-joda.git",
    Some("scm:git:git@github.com/aparo/scalajs-joda.git")))

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <developers>
    <developer>
      <id>aparo</id>
      <name>Alberto Paro</name>
      <url>https://github.com/aparo/</url>
    </developer>
  </developers>
)

pomIncludeRepository := { _ => false }
