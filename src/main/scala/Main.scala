import java.io.InputStream
import java.net.URL
import java.nio.file.{FileAlreadyExistsException, Files, Paths}
import java.util.UUID

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.photos.Photo
import com.vk.api.sdk.objects.wall.WallpostAttachmentType
import com.vk.api.sdk.objects.wall.responses.GetResponse

/**
  * Created by time2die on 05.01.17
  */
object Main {
  def main(args: Array[String]): Unit = {
    val appId = 6065032
    val transportClient = new HttpTransportClient()
    val vk = new VkApiClient(transportClient)

    import com.vk.api.sdk.client.actors.ServiceActor
    val actor = new ServiceActor(appId, "c6a0e2f7a44275fda2c7e96299aff791eefc3c37e427c53fa4cc10dc7a4b1ad21d04d7223494928c8c3bd")

    val getResponse: GetResponse = vk.wall()
      .get(actor)
      .domain("personal_witches")
      .count(5)
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

    println("beforeDownload")

    postLinks.par.foreach(i =>
      i._2.foreach(pI => pI.foreach(downloadFile))
    )
    println("end")
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


