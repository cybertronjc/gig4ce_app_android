package com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar

import com.gigforce.app.modules.custom_gig_preferences.UnavailableDataModel
import java.util.*

class VerticalCalendarDataItemModel (
    var title: String,
    val subTitle: String,
    val day: String,
    val isToday: Boolean,
    val isPreviousDate: Boolean,
    var isGigAssign: Boolean,
    val isMonth: Boolean,
    val year : Int,
    val month : Int,
    val date: Int,
    val monthStr:String,
    var isUnavailable:Boolean = false,
    var reason:String = ""
){
    fun getDateObj():Date{
        var dateObj = Date()
        dateObj.date = date
        dateObj.month = month
        dateObj.year = year
        return dateObj
    }
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
                getMonth(calendar.get(Calendar.MONTH)),
                false
            )
        }
        fun getIfNoGigFoundObject(calendar: Calendar,isPreviousDate: Boolean,isToday: Boolean,customPreferenceUnavailableData : ArrayList<UnavailableDataModel>): VerticalCalendarDataItemModel {
            var isPreviousDateFound:Boolean = isPreviousDate
            if(isToday){
                isPreviousDateFound = false
            }
            var dataModel = VerticalCalendarDataItemModel(
                "No gigs assigned",
                "",
                getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                isToday,
                isPreviousDateFound,
                false,
                false,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE),
                "",
                false
            )
            for(model in customPreferenceUnavailableData){
                if(calendar.get(Calendar.DATE)==model.date.date && calendar.get(Calendar.MONTH)==model.date.month && calendar.get(Calendar.YEAR)==model.date.year){
                    dataModel.isUnavailable = true
                    break;
                }
            }
            return dataModel
        }

        fun getDetailedObject(
            subTitle: String,
            countGigs: String,
            calendar: Calendar,
        isPreviousDate: Boolean,
            isToday: Boolean,customPreferenceUnavailableData : ArrayList<UnavailableDataModel>): VerticalCalendarDataItemModel {
            var isPreviousDateFound:Boolean = isPreviousDate
            if(isToday){
                isPreviousDateFound = false
            }
            var dataModel = VerticalCalendarDataItemModel(
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
                "",
                false
            )
            for(model in customPreferenceUnavailableData){
                if(calendar.get(Calendar.DATE)==model.date.date && calendar.get(Calendar.MONTH)==model.date.month && calendar.get(Calendar.YEAR)==model.date.year){
                    dataModel.isUnavailable = true
                    break;
                }
            }
            return dataModel
        }

        fun getMonth(month: Int): String {
            when (month) {
                0 -> return "January"
                1 -> return "February"
                2 -> return "March"
                3 -> return "April"
                4 -> return "May"
                5 -> return "June"
                6 -> return "July"
                7 -> return "August"
                8 -> return "September"
                9 -> return "October"
                10 -> return "November"
                11 -> return "December"
            }
            return ""
        }

        fun getDayOfWeek(dayofweek: Int): String {
            when (dayofweek) {
                1 -> return "Sun"
                2 -> return "Mon"
                3 -> return "Tues"
                4 -> return "Wed"
                5 -> return "Thur"
                6 -> return "Fri"
                7 -> return "Sat"
            }
            return ""
        }

    }
}