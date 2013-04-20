package org.googolplex
package snippet

import org.googolplex.SearchEngine
import net.liftweb.util._
import Helpers._
import net.liftweb.http.S
import com.redis.RedisClient

class Searcher {

  val query = S.param("r") openOr ""

  def searchPages = "#results" #> results(query)

  def searchImages = "#results " #> resultsImages(query)

  def results(query: String) = {
    val redis = new RedisClient("192.168.56.100", 6379)

    val res = SearchEngine.searchPages(query, redis).map {
      case (url, title) =>
        <li>
          <a target="_new" href={url} class="res">
            {title}
          </a>
        </li>
    }

    <span>Total:
      {res.length}
    </span> :: res
  }


  def resultsImages(query: String) = {
    val redis = new RedisClient("192.168.56.100", 6379)

    val res = SearchEngine.searchImages(query, redis).map {
      case (url, title) =>
        <li>
          <a target="_new" href={url} class="res">
            <img src={url}/>
          </a>
        </li>
    }

    <span>Total:
      {res.length}
    </span> :: res

  }


}

