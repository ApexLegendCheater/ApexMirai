import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.BotConfiguration

object WithConfiguration {
    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {
        val qq: Long = System.getenv("QQ")?.toLongOrNull() ?: error("Environment variable QQ is not set")
        val qqPsw: String = System.getenv("PSW") ?: error("Environment variable QQ PASSWORD is not set")
        val admin: Long = System.getenv("ADMIN")?.toLongOrNull() ?: error("Environment variable Admin is not set")
        // 使用自定义配置
        val bot = BotFactory.newBot(qq, qqPsw) {
            fileBasedDeviceInfo() // 使用 device.json 存储设备信息
            protocol = BotConfiguration.MiraiProtocol.IPAD // 切换协议
        }.alsoLogin()

        bot.getFriend(admin)?.sendMessage("Hello, World!")
        bot.eventChannel.subscribeAlways<FriendMessageEvent> {
            if (sender.id == admin) {
                val responseMsg = aiMsg(message.content)
                subject.sendMessage(message.quote() + responseMsg)
            }
        }
    }
}