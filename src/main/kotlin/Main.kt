import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.BotConfiguration

object WithConfiguration {
    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {
        val qq: Long = System.getenv("QQ")?.toLongOrNull() ?: error("Environment variable QQ is not set")
        val qqPsw: String = System.getenv("PSW") ?: error("Environment variable QQ PASSWORD is not set")
        val admin: Long = System.getenv("ADMIN")?.toLongOrNull() ?: error("Environment variable Admin is not set")
        val proxy: String = System.getenv("PROXY") ?: ""
        val proxyList: List<String> = proxy.split(',')
        // 使用自定义配置
        val bot = BotFactory.newBot(qq, qqPsw) {
            fileBasedDeviceInfo() // 使用 device.json 存储设备信息
            protocol = BotConfiguration.MiraiProtocol.IPAD // 切换协议
        }.alsoLogin()

        bot.getFriend(admin)?.sendMessage("Hello, World!")
        bot.eventChannel.subscribeAlways<FriendMessageEvent> {
            if (sender.id == admin || proxyList.contains(sender.id.toString())) {
                if (message.content.startsWith("ag授权")) {
                    val msgSplit: List<String> = message.content.split(" ")
                    if (msgSplit[0].trim() == "ag授权" && msgSplit.size == 5) {
                        val responseMsg =
                            addAuth(msgSplit[1].trim(), msgSplit[2].trim(), msgSplit[3].trim(), msgSplit[4].trim())
                        subject.sendMessage(message.quote() + responseMsg)
                    } else {
                        subject.sendMessage(message.quote() + "使用命令[ag授权 [脚本/ai/自动识别] [机器码] [yyyy-MM-dd/永久] [绑定qq号]]进行授权")
                    }
                } else {
                    val responseMsg = aiMsg(sender.id.toString(), message.content)
                    subject.sendMessage(message.quote() + responseMsg)
                }
            }
        }
        bot.eventChannel.subscribeAlways<GroupMessageEvent> {
            val content = message[1]
            if (content is At && content.target == qq) {
                val responseMsg = aiMsg(sender.id.toString(), message[2].content)
                if (responseMsg.isNotEmpty()) {
                    subject.sendMessage(message.quote() + responseMsg)
                }
            } else if (content is At && message.size == 3) {
                if (message[2].toString().trim() == "查询ag授权") {
                    subject.sendMessage(message.quote() + queryExpirationTime("qq", content.target.toString()))
                }
            } else {
                val messageSplit: List<String> = message.content.split(" ")
                if (messageSplit.size == 2 && messageSplit[0].trim() == "查询ag授权") {
                    subject.sendMessage(message.quote() + queryExpirationTime("machine_code", messageSplit[1]))
                }
            }
        }
    }
}