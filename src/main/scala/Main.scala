import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.{ServiceActor, UserActor}
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.wall.responses.GetResponse

/**
  * Created by time2die on 05.01.17
  */
object Main {
  def main(args: Array[String]): Unit = {
    val appId = 6065032
    val sid = "k5LdcZHcoG89FWi0HAQf"
    val transportClient = new HttpTransportClient()
    val vk = new VkApiClient(transportClient)

    import com.vk.api.sdk.client.actors.ServiceActor
//    val authResponse = vk.oauth.serviceClientCredentialsFlow(appId, sid).execute

//    val actor = new ServiceActor(appId, authResponse.getAccessToken)
    val actor = new ServiceActor(appId, "c6a0e2f7a44275fda2c7e96299aff791eefc3c37e427c53fa4cc10dc7a4b1ad21d04d7223494928c8c3bd")


    val APP_ID: String =
    "6065032" // ID вашего приложения

    val PERMISSIONS: String =
    "messages,status,wall,offline" // http://vk.com/dev/permissions

    val REDIRECT_URI: String =
    "https://oauth.vk.com/blank.html" // Заглушка для Standalone-приложений

    val DISPLAY: String =
    "mobile" // page|popup|mobile

    val API_VERSION: String =
    "5.5" // Последняя на данный момент

    val RESPONSE_TYPE: String =
    "token" // Есть ещё code, но это для сайтов


    val link: String =
    "http://oauth.vk.com/authorize?" + "client_id=" + APP_ID + "&scope=" + PERMISSIONS + "&redirect_uri=" + REDIRECT_URI + "&display=" + DISPLAY + "&v=" + API_VERSION + "&response_type=" + RESPONSE_TYPE

//    c6a0e2f7a44275fda2c7e96299aff791eefc3c37e427c53fa4cc10dc7a4b1ad21d04d7223494928c8c3bd&expires_in=0&user_id=228345

    val getResponse:GetResponse = vk.wall()
      .get(actor)
        .domain("overhear")
      .count(10)
      .offset(0)
      .execute();

    println("end")
  }
}


