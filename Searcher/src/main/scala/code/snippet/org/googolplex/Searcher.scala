package org.googolplex
package snippet

import net.liftweb.util._
import Helpers._
import net.liftweb.http.S

class Searcher {

  val request = S.param("r") openOr ""

  def results = if(request == "") "No request" else "No results for " + request

  def search = "#results" #> results
}

