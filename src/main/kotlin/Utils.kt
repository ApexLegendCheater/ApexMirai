import java.time.LocalDate

fun calculateNewExpirationTime(type: Int, currentExpirationTime: LocalDate): LocalDate? {
    return when (type) {
        0 -> null   // 体验卡
        1 -> currentExpirationTime.plusDays(1)   // 天卡
        2 -> currentExpirationTime.plusWeeks(1)  // 周卡
        3 -> currentExpirationTime.plusMonths(1) // 月卡
        4 -> currentExpirationTime.plusYears(1)  // 年卡
        5 -> null  // 永久
        else -> currentExpirationTime            // 默认不变
    }
}