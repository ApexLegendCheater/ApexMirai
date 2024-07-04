import org.ktorm.database.Database

val JDBC_URL: String = System.getenv("JDBC_URL") ?: System.getProperty("JDBC_URL") ?: error("JDBC_URL is not set")
val JDBC_USER: String = System.getenv("JDBC_USER") ?: System.getProperty("JDBC_USER") ?: error("JDBC_USER is not set")
val JDBC_PSW: String = System.getenv("JDBC_PSW") ?: System.getProperty("JDBC_PSW") ?: error("JDBC_USER is not set")
val database = Database.connect(JDBC_URL, user = JDBC_USER, password = JDBC_PSW)
