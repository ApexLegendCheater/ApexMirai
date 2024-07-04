import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.BotConfiguration

object Main {
    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {
        val qq: Long = System.getenv("QQ")?.toLongOrNull() ?: System.getProperty("QQ")?.toLongOrNull()
        ?: error("Environment variable QQ is not set")
        val qqPsw: String =
            System.getenv("PSW") ?: System.getProperty("PSW") ?: error("Environment variable QQ PASSWORD is not set")
        val admin: Long = System.getenv("ADMIN")?.toLongOrNull() ?: System.getProperty("ADMIN")?.toLongOrNull()
        ?: error("Environment variable Admin is not set")
        val proxy: String = System.getenv("PROXY") ?: System.getProperty("PROXY") ?: ""
        val proxyList: List<String> = proxy.split(',')
        // 使用自定义配置
        val bot = BotFactory.newBot(qq, qqPsw) {
            fileBasedDeviceInfo() // 使用 device.json 存储设备信息
            protocol = BotConfiguration.MiraiProtocol.IPAD // 切换协议
        }.alsoLogin()
        KtorServer.serverStart(bot)
        bot.getGroup(206666037L)?.members?.forEach {
            GroupMemberCache.addMemberToGroup(
                it.group.id, GroupMember(
                    it.id, it.nameCard, it.isAdministrator()
                )
            )
        }
        bot.getFriend(admin)?.sendMessage("Hello, World!")
        bot.eventChannel.subscribeAlways<FriendMessageEvent> {
            val responseMsg = OpenAi.aiMsg(sender.id.toString(), message.content)
            subject.sendMessage(message.quote() + responseMsg)
        }
        bot.eventChannel.subscribeAlways<GroupMessageEvent> {
            val content = message[1]
            if (content is At && content.target == qq) {
                val responseMsg = OpenAi.aiMsg(sender.id.toString(), message[2].content)
                if (responseMsg.isNotEmpty()) {
                    subject.sendMessage(message.quote() + responseMsg)
                }
            } else if (content is At && message.size == 3) {
                if (message[2].toString().trim() == "查询ag授权") {
                    subject.sendMessage(message.quote() + getAuthStr("qq", content.target.toString()))
                }
            } else {
                val regex = "获取(.*)体验卡".toRegex()
                val match = regex.find(message.content)
                if (match != null) {
                    val validateTypeStr = match.groupValues[1]
                    subject.sendMessage(
                        message.quote() + createExperienceCardByQQ(
                            sender.id.toString(),
                            validateTypeStr
                        )
                    )
                }
            }
        }
    }
}