package ie.tus.rocksolid.utils

import java.text.SimpleDateFormat
import java.util.*

fun getWeekStartDate(date: Date): String {
    val cal = Calendar.getInstance()
    cal.time = date
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(cal.time)
}
