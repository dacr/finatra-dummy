name := "finatra-dummy"
version := "1.0"
organization := "fr.janalyse"
scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

fork := true

javaOptions in run := Seq(
 "-Xms2g",
 "-Xmx2g",
 "-Xmn1000m",
 //"-XX:GCTimeRatio=50",
 //"-XX:SurvivorRatio=4",
 "-XX:+PerfDisableSharedMem", // Decrease the latency !!! 
 "-XX:+UseConcMarkSweepGC",
 "-XX:+UseParNewGC",
 "-XX:+CMSParallelRemarkEnabled",
 "-XX:+ScavengeBeforeFullGC",
 "-XX:+CMSScavengeBeforeRemark",
 "-XX:+ExplicitGCInvokesConcurrent",
 "-XX:+CMSClassUnloadingEnabled",
 "-XX:+UseCMSInitiatingOccupancyOnly",
 "-XX:CMSInitiatingOccupancyFraction=80",
// "-XX:ParallelGCThreads=3", // Number of CPU / 2 if load test injection is done from the same host
 "-XX:+AggressiveOpts",
 "-XX:+OptimizeStringConcat",
 "-XX:+UseFastAccessorMethods",
 "-XX:+UseThreadPriorities",
 "-XX:ThreadPriorityPolicy=42",
 "-verbose:gc",
 "-XX:+PrintGCDetails",
 "-XX:+PrintGCDateStamps",
 "-Xloggc:GC_finatra.log",
 "-Dcom.sun.management.jmxremote.port=2555",
 "-Dcom.sun.management.jmxremote.authenticate=false",
 "-Dcom.sun.management.jmxremote.ssl=false",
 "-Djava.net.preferIPv4Stack=true",
 "-Djava.net.preferIPv6Addresses=false",
 "-Djava.security.egd=file:///dev/urandom",
 "-Dhazelcast.jmx=true"
)

/*
javaOptions in run := Seq(
 "-Xms2g",
 "-Xmx2g",
 "-XX:+UseG1GC",
 "-XX:MaxGCPauseMillis=50",
 "-XX:G1HeapRegionSize=200m",
 "-XX:InitiatingHeapOccupancyPercent=75",
 "-XX:+ParallelRefProcEnabled",
 "-XX:+PerfDisableSharedMem",
 "-XX:+AggressiveOpts",
 "-XX:+OptimizeStringConcat",
 "-verbose:gc",
// "-XX:+PrintGCDetails",
 "-XX:+PrintGCDateStamps",
 "-Xloggc:GC_finatra.log",
 "-Dcom.sun.management.jmxremote.port=2555",
 "-Dcom.sun.management.jmxremote.authenticate=false",
 "-Dcom.sun.management.jmxremote.ssl=false",
 "-Djava.net.preferIPv4Stack=true",
 "-Djava.net.preferIPv6Addresses=false",
 "-Dhazelcast.jmx=true"
)
*/

libraryDependencies ++= Seq(
  "com.twitter"         %% "finatra-http"                         % "2.6.0",
  "ch.qos.logback"       % "logback-classic"                      % "1.1.7",
  "org.codehaus.janino"  % "janino"                               % "2.7.8" // Allow logback config file conditionals
).map(
   _.exclude("org.scala-lang", "scala-compiler")
 //   .exclude("org.scala-lang", "scala-reflect")
)

/*
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

assemblyMergeStrategy in assembly := {
  case "BUILD" => MergeStrategy.discard
  case other => MergeStrategy.defaultMergeStrategy(other)
}
*/

sourceGenerators in Compile <+=
 (sourceManaged in Compile, version, name) map {
  (dir, version, projectname) =>
  val file = dir / "dummy" / "MetaInfo.scala"
  val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val buildate = sdf.format(new java.util.Date())
  IO.write(file,
  """package dummy
    |object MetaInfo {
    |  val version="%s"
    |  val project="%s"
    |  val buildate="%s"
    |}
    |""".stripMargin.format(version, projectname, buildate) )
  Seq(file)
}

