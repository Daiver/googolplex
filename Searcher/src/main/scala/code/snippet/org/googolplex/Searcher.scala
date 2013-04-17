package org.googolplex
package snippet

import org.googolplex.SearchEngine
import net.liftweb.util._
import Helpers._
import net.liftweb.http.S
import com.redis.RedisClient
import xml.NodeSeq

class Searcher {
    object Int {
        def unapply(s : String) : Option[Int] = try {
            Some(s.toInt)
        } catch {
            case _ : java.lang.NumberFormatException => None
        }
    }
  def getValueFromString(s: String, default: Int): Int = s match {
    case "inf" => Integer.MAX_VALUE
    case Int(x) => x
    case _ => default
  }
  val query = S.param("r") openOr ""
  val startWith = getValueFromString(S.param("start") openOr "", 0)
  val offset = getValueFromString(S.param("offset") openOr "", 20)

  def getpages = "#pages" #> getPagesHrefs(query, startWith, offset)
  def getPagesHrefs(query:String, startWith: Int, offset: Int) = {
    val limit = 20
    val startPage = startWith/offset
    val leftindex = if (startPage - limit / 2 > 0) startPage - limit / 2 else 0
    val res = for( i <- List.range(leftindex, leftindex + limit)) yield <a href={"/search?r=%s&start=%d&offset=%d".format(query, (leftindex+i)*offset, offset)} >{i + 1} </a>
    res
  }

  def search = "#results" #> results(query, startWith, offset)

  def results(query: String, startWith: Int, offset: Int) = {
    val searchEngine = new SearchEngine
    val redis = new RedisClient("localhost", 6379)
    var i = 0

    val tmpres = searchEngine.search(query, redis)
    val lastIndex = if(offset > 0) offset + startWith else tmpres.length - startWith
    val res = tmpres.slice(startWith, lastIndex).map{
      case (url, title) =>
        <li>
          <a target="_new" href={url} class="res">
            {title}
          </a>
        </li>
    }

    <span><b>{query} </b> Total:
      {res.length}
    </span> :: res
  }

}

