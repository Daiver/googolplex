organization := "org.plex"

name := "googolplex"

version := "0.1-SNAPSHOT"

seq(com.github.siasia.WebPlugin.webSettings :_*)

libraryDependencies ++= {
  val liftVersion = "2.5-M4"
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,test"
  )
}
