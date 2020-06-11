package com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar

import java.util.*

class VerticalCalendarDataItemModel (
    val title: String,
    val subTitle: String,
    val day: String,
    val isToday: Boolean,
    val isPreviousDate: Boolean,
    val isGigAssign: Boolean,
    val isMonth: Boolean,
    val year : Int,
    val month : Int,
    val date: Int,
    val monthStr:String
){
    companion object {
        fun getMonthObject(calendar:Calendar):VerticalCalendarDataItemModel{
            return VerticalCalendarDataItemModel(
                "",
                "",
                getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                false,
                false,
                false,
                true,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                getMonth(calendar.get(Calendar.MONTH))
            )
        }
        fun getIfNoGigFoundObject(calendar: Calendar,isPreviousDate: Boolean,isToday: Boolean): VerticalCalendarDataItemModel {
            var isPreviousDateFound:Boolean = isPreviousDate
            if(isToday){
                isPreviousDateFound = false
            }
            return VerticalCalendarDataItemModel(
                "No gigs Assigned",
                "",
                getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                isToday,
                isPreviousDateFound,
                false,
                false,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                ""
            )
        }

        fun getDetailedObject(
            subTitle: String,
            countGigs: String,
            calendar: Calendar,
        isPreviousDate: Boolean,
            isToday: Boolean): VerticalCalendarDataItemModel {
            var isPreviousDateFound:Boolean = isPreviousDate
            if(isToday){
                isPreviousDateFound = false
            }
            return VerticalCalendarDataItemModel(
                subTitle,
                countGigs,
                getDayOfWeek(
                    calendar.get(
                        Calendar.DAY_OF_WEEK
                    )
                ),
                isToday,
                isPreviousDateFound,
                true,
                false,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                ""
            )
        }

        fun getMonth(month: Int): String {
            when (month) {
                0 -> return "Jan"
                1 -> return "Feb"
                2 -> return "Mar"
                3 -> return "Apr"
                4 -> return "May"
                5 -> return "Jun"
                6 -> return "Jul"
                7 -> return "Aug"
                8 -> return "Sep"
                9 -> return "Oct"
                10 -> return "Nov"
                11 -> return "Dec"
            }
            return ""
        }

        fun getDayOfWeek(dayofweek: Int): String {
            when (dayofweek) {
                1 -> return "SUN"
                2 -> return "MON"
                3 -> return "TUE"
                4 -> return "WED"
                5 -> return "THU"
                6 -> return "FRI"
                7 -> return "SAT"
            }
            return ""
        }

    }
}