import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent
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
        val checkGroup: String = System.getenv("CHECK_GROUP") ?: System.getProperty("CHECK_GROUP") ?: ""
        val checkGroupList: List<String> = checkGroup.split(',')
        // 使用自定义配置
        val bot = BotFactory.newBot(qq, qqPsw) {
            fileBasedDeviceInfo() // 使用 device.json 存储设备信息
            protocol = BotConfiguration.MiraiProtocol.IPAD // 切换协议
        }.alsoLogin()
        bot.groups.map {
            if (checkGroupList.contains(it.id.toString())) {
                flushGroupMember(bot, it.id)
            }
        }
        KtorServer.serverStart(bot)
        bot.getFriend(admin)?.sendMessage("Hello, World!")
        bot.eventChannel.subscribeAlways<FriendMessageEvent> {
            val responseMsg = OpenAi.aiMsg(sender.id.toString(), message.content)
            subject.sendMessage(message.quote() + responseMsg)
        }
        bot.eventChannel.subscribeAlways<GroupMessageEvent> {
            val content = message[1]
            if (message.content == "查询ag授权") {
                subject.sendMessage(message.quote() + getAuthStr("qq", sender.id.toString()))
            } else if (content is At && content.target == qq) {
                val responseMsg = OpenAi.aiMsg(sender.id.toString(), message[2].content)
                if (responseMsg.isNotEmpty()) {
                    subject.sendMessage(message.quote() + responseMsg)
                }
            } else if (content is At && message.size == 3) {
                val msg: String = message[2].toString().trim()
                if (msg == "查询ag授权") {
                    subject.sendMessage(message.quote() + getAuthStr("qq", content.target.toString()))
                }
                val match = "授权(ai|自动识别|升级脚本)(天|月|周|年|永久)卡".toRegex().find(msg)
                if (match != null) {
                    if (sender.isOwner() || sender.isAdministrator()) {
                        val (cardType, duration) = match.destructured
                        val key: String = createKeys(cardType, duration, content.target.toString())
                        sender.sendMessage("授权给[${content.target}]的[${cardType}${duration}卡]，卡密为:[${key}]")
                        group.getMember(content.target)!!
                            .sendMessage("[${sender.id}]授权给你的[${cardType}${duration}卡]，卡密为:[${key}]")
                        subject.sendMessage(message.quote() + "卡密已私聊，请查收后妥善保管。")
                    } else {
                        subject.sendMessage(message.quote() + "违规操作！只有管理员有权授权！")
                    }
                }
            } else if (message.content == "/帮助" || message.content == "/help" || message.content == "/h") {
                subject.sendMessage(
                    message.quote() + "可用命令：\n1、查询ag授权\n" + "2、获取[ai/自动识别/升级脚本]体验卡"
                )
            } else {
                val match = "获取(.*)体验卡".toRegex().find(message.content)
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
        bot.eventChannel.subscribeAlways<MemberJoinEvent> {
            if (checkGroupList.contains(groupId.toString())) {
                println("${member.id}加入群${groupId}")
                GroupMemberCache.addMemberToGroup(
                    groupId, GroupMember(
                        member.id,
                        member.nameCard,
                        member.nick,
                        member.isAdministrator()
                    )
                )
            }

        }
        bot.eventChannel.subscribeAlways<MemberLeaveEvent> {
            if (checkGroupList.contains(groupId.toString())) {
                println("${member.id}离开群${groupId}")
                GroupMemberCache.removeMemberFromGroup(groupId, member.id)
            }
        }
    }

    private fun flushGroupMember(bot: Bot, groupId: Long) {
        println("刷新群${groupId}成员缓存")
        bot.getGroup(groupId)?.members?.forEach {
            GroupMemberCache.addMemberToGroup(
                it.group.id, GroupMember(
                    it.id, it.nameCard, it.nick, it.isAdministrator()
                )
            )
        }
    }
}