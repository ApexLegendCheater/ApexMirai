object GroupMemberCache {
    // 使用HashMap存储群组ID及其对应的群组成员列表
    private val groupCache: MutableMap<Long, MutableMap<Long, GroupMember>> = HashMap()

    // 添加成员到指定群组
    fun addMemberToGroup(groupId: Long, member: GroupMember) {
        // 获取或创建群组成员列表
        val members = groupCache.getOrPut(groupId) { HashMap() }
        // 将成员添加到群组成员列表
        members[member.id] = member
    }

    // 获取指定群组的成员列表
    fun getMembersInGroup(groupId: Long): MutableMap<Long, GroupMember>? {
        return groupCache[groupId]
    }

    // 从指定群组中移除成员
    fun removeMemberFromGroup(groupId: Long, memberId: Long) {
        groupCache[groupId]?.remove(memberId)
    }

    // 移除整个群组
    fun removeGroup(groupId: Long) {
        groupCache.remove(groupId)
    }

    // 打印当前缓存中的所有群组及其成员
    fun printGroupCache() {
        for ((groupId, members) in groupCache) {
            members.forEach {
                println("Group ID: $groupId, Members: ${it.key}")
            }
        }
    }

    fun memberInAnyGroup(memberId: Long): Boolean {
        for ((_, members) in groupCache) {
            if (members.contains(memberId)) {
                return true
            }
        }
        return false
    }
}