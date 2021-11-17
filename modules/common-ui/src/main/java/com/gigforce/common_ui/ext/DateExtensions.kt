package com.gigforce.common_ui.ext

import java.time.LocalDate
import java.time.ZoneId
import java.util.*


fun LocalDate.toDate() : Date{
   return Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant());
}
