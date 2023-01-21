lazy val root = (project in file("."))
  .settings(
    name := "tenzo-experiment",
    organization := "io.github.todokr",
    version := "2023.1.0",
    scalaVersion := "2.13.10",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Xlint",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused"
    ),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "fastparse" % "2.3.3",
      "com.lihaoyi" %% "utest" % "0.8.1" % "test",
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )

description := ""
licenses := List("EPL 2.0" -> new URL("https://www.eclipse.org/legal/epl-2.0/"))
homepage := Some(url("https://github.com/todokr/tenzo"))
developers := List(
  Developer(
    id = "todokr",
    name = "Shunsuke Tadokoro",
    email = "s.tadokoro0317@gmail.com",
    url = url("https://github.com/todokr")
  )
)
scmInfo := Some(
  ScmInfo(
    url("https://github.com/todokr/tenzo"),
    "scm:git@github.com:todokr/tenzo.git"
  )
)
pomIncludeRepository := { _ => false }
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true
credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials")
