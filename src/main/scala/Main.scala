import java.io.InputStream
import java.net.URL
import java.nio.file.{FileAlreadyExistsException, Files, Paths}
import java.util.UUID

import Main._
import com.typesafe.config.{Config, ConfigFactory}
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.photos.Photo
import com.vk.api.sdk.objects.wall.WallpostAttachmentType
import com.vk.api.sdk.objects.wall.responses.GetResponse
import org.telegram.telegrambots.api.methods.send.SendPhoto
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.{ApiContextInitializer, TelegramBotsApi}

import scala.util.Random

/**
  * Created by time2die on 05.01.17
  */
object Main {
  val conf: Config = ConfigFactory.load

  def main(args: Array[String]): Unit = {
    val appId = 6065032
    val transportClient = new HttpTransportClient()
    val vk = new VkApiClient(transportClient)

    import com.vk.api.sdk.client.actors.ServiceActor
    val actor = new ServiceActor(appId, conf.getString("tgBotKey"))

    val getResponse: GetResponse = vk.wall()
      .get(actor)
//      .domain("personal_witches")
      .domain("red_shine")
      .count(10500)
      .offset(0)
      .execute()

    import scala.collection.JavaConverters._
    val items = getResponse.getItems.asScala.toList
    val postAttachmentTouples = items
      .filter(i => i.getAttachments != null)
      .filter(i => i.getIsPinned == null)
      .map(i => (i, i.getAttachments.asScala.toList))
    val postPhotos = postAttachmentTouples.map(i => (i._1, i._2.filter(_.getType == WallpostAttachmentType.PHOTO))).map(i => (i._1, i._2.map(_.getPhoto)))
    val postLinks = postPhotos.map(i => (i._1, i._2.map(getMaxResolutionPhotos(_))))

    val links = postLinks.flatMap(_._2).map(_.get.url)
    //    postLinks.par.foreach(i =>
    //      i._2.foreach(pI => pI.foreach(downloadFile))
    //    )
    //    println("end")

    ApiContextInitializer.init()
    new TelegramBotsApi().registerBot(new PictureBot(links))

  }

  val resolution = List(2560, 1280, 807, 604, 130, 75)

  def getMaxResolutionPhotos(p: Photo): Option[PhotoContainer] = {
    List(p.getPhoto2560, p.getPhoto1280, p.getPhoto807, p.getPhoto604, p.getPhoto130, p.getPhoto75)
      .zip(resolution)
      .filter(_._1 != null)
      .sortWith(_._2 > _._2)
      .map(i => Option(PhotoContainer(i._1, i._2)))
      .head
  }


  def downloadFile(fileToDownload: PhotoContainer) {
    try {
      val fileName = s"pictures/${UUID.nameUUIDFromBytes(fileToDownload.url.getBytes()).toString}.jpg"
      println(s"start download ${fileToDownload.res}: ${fileToDownload.url} : $fileName")

      saveFile(fileToDownload.url, fileName)

      println(s"close download $fileName")
    } catch {
      case e: FileAlreadyExistsException => e.printStackTrace()
      case e: Throwable => e.printStackTrace()
    }
  }

  def saveFile(url: String, fileName: String) = {
    val in: InputStream = new URL(url).openStream()
    Files.copy(in, Paths.get(fileName))
  }


  def printAuthLink() = {
    val APP_ID: String = "6065032" // ID вашего приложения

    val PERMISSIONS: String = "messages,status,wall,offline" // http://vk.com/dev/permissions

    val REDIRECT_URI: String = "https://oauth.vk.com/blank.html" // Заглушка для Standalone-приложений

    val DISPLAY: String = "mobile" // page|popup|mobile

    val API_VERSION: String = "5.5" // Последняя на данный момент

    val RESPONSE_TYPE: String =
      "token" // Есть ещё code, но это для сайтов

    val link: String =
      s"http://oauth.vk.com/authorize?client_id=$APP_ID&scope=$PERMISSIONS&redirect_uri=$REDIRECT_URI&display=$DISPLAY&v=$API_VERSION&response_type=$RESPONSE_TYPE"
  }

  case class PhotoContainer(url: String, res: Integer)

}

class PictureBot(links: List[String]) extends TelegramLongPollingBot {

  val rnd: Random = new Random()

  println(s"start bot\nphotos:${links.size}")

  override def getBotUsername: String = "tRussianBank"

  override def getBotToken: String = conf.getString("tgBotKey")

  override def onUpdateReceived(update: Update): Unit = {
    val photoUrl = links(rnd.nextInt(links.length))
    val in: InputStream = new URL(photoUrl).openStream()
    val sp = new SendPhoto()
      .setChatId(update.getMessage.getChatId)
      .setNewPhoto(UUID.randomUUID().toString, in)

    sendPhoto(sp)
  }
}



