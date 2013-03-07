package bootstrap.liftweb

import net.liftweb.http.{Html5Properties, LiftRules, Req}

class Boot {
  def boot {
    LiftRules.addToPackages("org.plex.googolplex")

    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    LiftRules.enableLiftGC = false
  }
}
