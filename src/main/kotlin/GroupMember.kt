import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(val id: Long, val name: String, val isAdmin: Boolean)