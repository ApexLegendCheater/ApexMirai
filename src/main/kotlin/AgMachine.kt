import org.ktorm.entity.Entity
import java.time.LocalDate

interface AgMachine : Entity<AgMachine> {
    companion object : Entity.Factory<AgMachine>()

    val id: Int
    var machineCode: String
    var accessGranted: String
    var expirationTime: LocalDate?
    var qq: String
    var validateType: String
}