organization  := "com.ojk"

version       := "0.1"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= {
  val akkaV = "2.2.3"
  val sprayV = "1.2.0"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-httpx"   % sprayV,
    "io.spray"            %   "spray-client"  % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.7" % "test",
    "com.github.seratch"  %%  "scalikesolr"   % "[4.5,)",
    "org.json4s"          %%  "json4s-native" % "3.2.7",
    "org.slf4j"           %  "slf4j-log4j12" % "1.7.6"
  )
}

seq(Revolver.settings: _*)
