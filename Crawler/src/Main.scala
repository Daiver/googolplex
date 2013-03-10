import java.net.URL
import java.util.regex.Pattern
import java.security.MessageDigest
import ru.kolyvan.redis.Conv._
import ru.kolyvan.redis.Redis
import scala.util.Marshal
import java.io._
//import com.redis._
//import Conv._

object Main {

    def redis_tst() = {
        //val r = new RedisClient("localhost", 6379)
        val r = Redis()
        //println(r.set("key 2", B("KILL THEM ALL")))
        //println(r.set("pages:globalindex", B(0)))
        //println(r.set("key2", B(1024)))

        //println(r.flushdb()) //DO NOT USE THIS!!!!
        //println("before print")

        val index = S(r.get("pages:globalindex"))
        println(index)

        //println(I(r.get("pages:globalindex")))
        //println()
        for(x <-  r.keys("*"))
            println(x)

        //println(rcl.keys("*").get)
    }

    def reset_db(dbclient: Redis) = {
        dbclient.flushdb()
        dbclient.set("pages:globalindex", B(0))
        dbclient.incr("pages:globalindex")
    }

    def main(args: Array[String]) = {

        val dbc = Redis("localhost", 6379)
        //reset_db(dbc)
        //redis_tst()

        val crawler = new Crawler()
        val major_url = "http://habrahabr.ru/"
        val search_depth = 3

        //val pages =
        crawler.grabHost(major_url, dbc, search_depth)
        //println("Index size: " + pages.size)
        println("Ready to search")
        val se = new SearchEngine()
        var ok = true

        //pages(major_url).saveIntoDB(dbc)
        while (ok) {
            println("Type query:")
            val ln = {
                readLine()
            }
            ok = ln != null
            if (ok) {
                //se.search(ln, pages).foreach((x: StoredPage) => println(x.URL))
                se.search(ln, dbc).foreach((x: String) => println(x))
            }
        }
        //search("java", pages).foreach((x:StoredPage) => println(x.URL))
    }
}