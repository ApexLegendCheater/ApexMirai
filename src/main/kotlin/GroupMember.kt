import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(val id: Long, val nameCard: String, val nick: String, val isAdmin: Boolean)