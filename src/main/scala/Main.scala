import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.{ServiceActor, UserActor}
import com.vk.api.sdk.httpclient.HttpTransportClient

/**
  * Created by time2die on 05.01.17
  */
object Main {
  def main(args: Array[String]): Unit = {
    val appId = 6065032
    val sid = "k5LdcZHcoG89FWi0HAQf"
    val sToken = "f1f39d26f1f39d26f1f39d262df1af16aeff1f3f1f39d26a8cbb944ce0db8d1e161a540"
    val serviceActor = new ServiceActor(appId,sid,sToken)
    val transportClient = new HttpTransportClient()
    val vk = new VkApiClient(transportClient)
    println(vk.getVersion)
  }
}


