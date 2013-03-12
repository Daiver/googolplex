package org.googolplex
package snippet

import org.googolplex.SearchEngine
import net.liftweb.util._
import Helpers._
import net.liftweb.http.S
import ru.kolyvan.redis.Redis

class Searcher {

  val query = S.param("r") openOr ""

  def search = "#results" #> results(query)

  def results(query: String) = {
      val searchEngine = new SearchEngine
      val redis = Redis("localhost", 6379)
      var i = 0

      val res = searchEngine.search(query, redis).reverse.map(x => <li><a target="_new" href={x} class="res">{x}</a></li>)

      <span>Total: {res.length}</span> :: res
  }

}

