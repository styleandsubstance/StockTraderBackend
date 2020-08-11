name := """StockTrader"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
  "org.squeryl" % "squeryl_2.11" % "0.9.6-RC4",
  "net.ruippeixotog" %% "scala-scraper" % "1.2.0",
  "org.apache.commons" % "commons-email" % "1.4",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.5.0-akka-2.4.x"
)
libraryDependencies += evolutions
libraryDependencies += filters

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


//fork in run := true