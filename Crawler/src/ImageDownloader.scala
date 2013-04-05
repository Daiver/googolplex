package org.googolplex.crawler

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.net.URL
import actors.Actor
import collection.immutable.HashMap
import java.util.Date
import com.redis.RedisClient

class ImageDownloader(databaseClient: RedisClient) extends Actor {

  abstract class ImageDownloaderMessage
  case class Exit()
  case class Process(imageUrl: String, pageUrl: String, keywords: HashMap[String, Int])

  def act() {
    var isRunning = true
    while (isRunning)
      receive {
        case Exit => isRunning = false
        case Process(imageUrl, pageUrl, keywords) => {
          val grabDate = new Date
          val imageKey = "image:" + imageUrl

          if (databaseClient.exists(imageKey)) {
            databaseClient.hset(imageKey, "date", grabDate)
          } else {
            val hash = hashImage(imageUrl)
            val imageIndex = databaseClient.incr("images:globalindex")
            val imageInfo = HashMap("id" -> imageIndex, "hash" -> hash, "date" -> grabDate)
            databaseClient.hmset(imageKey, imageInfo)
            databaseClient.sadd("image:hash:" + hash, imageUrl)
          }
          databaseClient.sadd("image:pages:" + imageUrl, pageUrl)
          keywords.foreach {
            case (word, count) =>
              databaseClient.zadd("image:keyword:" + word, count, imageUrl)
          }
        }
      }
  }

  private def hashImage(imageUrl: String): BigInt = {
    val image = ImageIO.read(new URL(imageUrl))
    hashImage(image)
  }

  private def hashImage(image: BufferedImage): BigInt = {
    def foldPixels(image: BufferedImage, f: (BigInt, Int) => BigInt) = {
      val w = image.getWidth
      val h = image.getHeight
      def fold(i: Int, j: Int, acc: BigInt): BigInt =
        if (i == h) acc
        else if (j == w) fold(i + 1, 0, acc)
        else fold(i, j + 1, f(acc, image.getRGB(i, j)))

      fold(0, 0, 0)
    }
    val normalizedImage = {
      val w = 8
      val h = w
      val scaledGrayImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY)
      scaledGrayImage.createGraphics().drawImage(image, 0, 0, w, h, null)
      scaledGrayImage
    }
    val imageSize = normalizedImage.getWidth * normalizedImage.getHeight
    val averagePixelColor = foldPixels(normalizedImage, (s, c) => s + c) / imageSize
    def hash(h: BigInt, c: Int) = (h << 1) + (if (c < averagePixelColor) 0 else 1)

    foldPixels(normalizedImage, hash)
  }


}
