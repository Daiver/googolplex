package org.googolplex

import collection.mutable
import java.util.StringTokenizer
import ru.kolyvan.redis
import ru.kolyvan.redis.Redis
import ru.kolyvan.redis.Conv._

class SearchEngine {

  def search(query: String, databaseClient: Redis) = {
    val keyWords = {
      val keyWordsTokenizer = new StringTokenizer(query.toLowerCase, " \n,.:;\"'/\\+-=!@#$%^&*|_()<>{}[]~`\t", true)
      def toList(res: List[String] = List()): List[String] =
        if (keyWordsTokenizer.hasMoreTokens) toList(keyWordsTokenizer.nextToken() :: res)
        else res

      toList()
    }

    val results = new mutable.LinkedHashMap[String, Float]()
    keyWords.foreach((x: String) => {
      databaseClient.zrange2("pages:KW:" + x, 0, -1).map(
        (data: (redis.Bytes, Float)) => (S(data._1), data._2)).foreach {
        ((d: (String, Float)) => if (results.contains(d._1)) results(d._1) += d._2
        else results(d._1) = d._2)
      }
    })
    results.toList.sortBy(_._2).map(_._1)
  }

  /*def search(query: String, pages: scala.collection.mutable.HashMap[String, StoredPage]) = {
      val tokenizer = CrawlerSupport.get_splited_page(query)
      def tokenizer_toList(res: List[String] = List()): List[String] = {
          if (tokenizer.hasMoreTokens()) {
              tokenizer_toList(res :+ tokenizer.nextToken())
          } else {
              res
          }
      }

      val keyWords = tokenizer_toList()
      def filterFunc(page: StoredPage, words: List[String]): Boolean = {
          if (words.length == 0) {
              return true
          }
          else {
              if (!page.keyWords.contains(words.head)) false
              else (filterFunc(page, words.tail))
          }
      }

      def freqRange(x: StoredPage) = {
          x.keyWords.filter((x: (String, Int)) => keyWords.contains(x._1)).values.sum
      }

      val listtosort = pages.filter((x: (String, StoredPage)) => filterFunc(x._2, keyWords)).values.toList
      listtosort sortWith {
          freqRange(_) > freqRange(_)
      }
  }

  def SerialisePages(fname: String, foo: scala.collection.mutable.HashMap[String, StoredPage]) = {
    val out = new FileOutputStream(fname)
    out.write(Marshal.dump(foo.values.toList))
    out.close
  }


  def DeSerialisePages(fname: String) = {
    val in = new FileInputStream(fname)
    val bytes = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray
    val lst = Marshal.load[List[StoredPage]](bytes)
    var pages = scala.collection.mutable.HashMap[String, StoredPage]()
    for (p <- lst) {
      pages.put(p.URL, p)
    }
    pages
  }
   */
}