import java.net.URL
import java.util.regex.Pattern
import java.security.MessageDigest
import scala.util.Marshal
import java.io._


object Main  {
  def search(query : String, pages : scala.collection.mutable.HashMap[String, StoredPage]) = {
    val tokenizer = CrawlerSupport.get_splited_page(query)
    def tokenizer_toList(res: List[String] = List()): List[String] = {
      if (tokenizer.hasMoreTokens()) {
        tokenizer_toList(res :+ tokenizer.nextToken())
      } else {
        res
      }
    }

    val keyWords = tokenizer_toList()
    def filterFunc(page : StoredPage, words: List[String]) : Boolean = {
      if (words.length == 0) {
        return true
      }
      else {
        if (! page.keyWords.contains(words.head)) false
        else (filterFunc(page, words.tail))
      }
    }

    def freqRange(x : StoredPage) = {
      x.keyWords.filter((x : (String, Int)) => keyWords.contains(x._1)).values.sum
    }

    val listtosort = pages.filter((x : (String, StoredPage)) => filterFunc(x._2, keyWords)).values.toList
    listtosort sortWith {freqRange(_) > freqRange(_) }
  }

  def SerialisePages(fname : String, foo : scala.collection.mutable.HashMap[String, StoredPage]) = {
    val out = new FileOutputStream(fname)
    out.write(Marshal.dump(foo.values.toList))
    out.close
  }

  def DeSerialisePages(fname : String) = {
    val in = new FileInputStream(fname)
    val bytes = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray
    val lst = Marshal.load[List[StoredPage]](bytes)
    var pages = scala.collection.mutable.HashMap[String, StoredPage]()
    for(p <- lst) {
      pages.put(p.URL, p)
    }
    pages
  }

  def main(args : Array[String]) = {
    def grabHost(major_url : String, max_depth : Int = 1) = {
      val crawler = new Crawler()
      var pages = scala.collection.mutable.HashMap[String, StoredPage]()
      println("Start grabing " + major_url)
      def walker(url : String, depth : Int) : Unit = {
        if (!pages.contains(url)) {
          val page = crawler.grabUrl(url)
          println(pages.size  + " walking page url " + page.URL + "  num of hrefs " + page.links.length)
          pages.put(url, page)
          if (depth < max_depth)
            page.links.filter(_.startsWith(major_url)).par.foreach((x:String) => {
              try {
                walker(x, depth+1)
              } catch {
                case e: Exception => println(e)
              }
            })
        } else {
          println("passing " + url)
        }
      }
      walker(major_url, 0)
      pages
    }
    val major_url = "http://habrahabr.ru/"
    val search_depth = 1
    val pages = grabHost(major_url, search_depth)
    println("Index size: " + pages.size)
    println("Ready to search")
    var ok = true
    while( ok ) {
      println("Type query:")
      val ln = {
        readLine()
      }
      ok = ln != null
      if( ok ) {
        search(ln, pages).foreach((x:StoredPage) => println(x.URL))

      }
    }
    //search("java", pages).foreach((x:StoredPage) => println(x.URL))
  }
}
