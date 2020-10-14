package com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar

import com.gigforce.app.core.toLocalDate
import com.gigforce.app.modules.custom_gig_preferences.UnavailableDataModel
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import java.time.LocalDate
import java.util.*

class VerticalCalendarDataItemModel(
    var title: String,
    var subTitle: String,
    var gigCount : Int =  0,
    val day: String,
    val isToday: Boolean,
    val isPreviousDate: Boolean,
    var isGigAssign: Boolean,
    val isMonth: Boolean,
    val year: Int,
    val month: Int,
    val date: Int,
    val monthStr: String,
    var isUnavailable: Boolean = false,
    var reason: String = ""
) {
    fun getDateObj(): Date {

       return Calendar.getInstance().apply {
            set(Calendar.YEAR,year)
            set(Calendar.MONTH,month)
            set(Calendar.DAY_OF_MONTH,date)
        }.time
    }

    fun getLocalDate() : LocalDate{
        return LocalDate.of(year,month + 1,date)
    }

    companion object {
        fun getMonthObject(calendar: Calendar): VerticalCalendarDataItemModel {
            return VerticalCalendarDataItemModel(
                "",
                "",
                0,
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

        fun getIfNoGigFoundObject(
            calendar: Calendar,
            isPreviousDate: Boolean,
            isToday: Boolean,
            customPreferenceUnavailableData: ArrayList<UnavailableDataModel>,
            preferenceData: PreferencesDataModel?
        ): VerticalCalendarDataItemModel {
            var isPreviousDateFound: Boolean = isPreviousDate
            if (isToday) {
                isPreviousDateFound = false
            }
            var dataModel = VerticalCalendarDataItemModel(
                "No gigs assigned",
                "",
                0,
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

            if (preferenceData != null) {
                var isFound = false
                if(preferenceData.isweekdaysenabled)
                for (data in preferenceData.selecteddays) {
                    if (calendar.get(Calendar.DAY_OF_WEEK) == getDayOfWeek(data)) {
                        isFound = true
                        break
                    }
                }
                if (!isFound && preferenceData.isweekendenabled)
                    for (data in preferenceData.selectedweekends) {
                        if (calendar.get(Calendar.DAY_OF_WEEK) == getDayOfWeek(data)) {
                            isFound = true
                            break
                        }
                    }
                if (!isFound) {
                    dataModel.isUnavailable = true
                }
            }

            for (model in customPreferenceUnavailableData) {
                if (calendar.time.toLocalDate().equals(model.date.toLocalDate())
                ) {
                    dataModel.isUnavailable = model.dayUnavailable
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
            isToday: Boolean,
            customPreferenceUnavailableData: ArrayList<UnavailableDataModel>,
            preferenceData: PreferencesDataModel?
        ): VerticalCalendarDataItemModel {
            var isPreviousDateFound: Boolean = isPreviousDate
            if (isToday) {
                isPreviousDateFound = false
            }
            var dataModel = VerticalCalendarDataItemModel(
                subTitle,
                countGigs,
                0,
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

            if (preferenceData != null) {
                var isFound = false
                if(preferenceData.isweekdaysenabled)
                for (data in preferenceData.selecteddays) {
                    if (calendar.get(Calendar.DAY_OF_WEEK) == getDayOfWeek(data)) {
                        isFound = true
                        break
                    }
                }
                if (!isFound && preferenceData.isweekendenabled)
                    for (data in preferenceData.selectedweekends) {
                        if (calendar.get(Calendar.DAY_OF_WEEK) == getDayOfWeek(data)) {
                            isFound = true
                            break
                        }
                    }
                if (!isFound) {
                    dataModel.isUnavailable = true
                }
            }
            for (model in customPreferenceUnavailableData) {
                if (calendar.time.toLocalDate().equals(model.date.toLocalDate())) {
                    dataModel.isUnavailable = model.dayUnavailable
                    break;
                }
            }

            return dataModel
        }

        fun getDayOfWeek(day: String): Int {
            when (day) {
                "Sunday" -> return 1
                "Monday" -> return 2
                "Tuesday" -> return 3
                "Wednesday" -> return 4
                "Thursday" -> return 5
                "Friday" -> return 6
                "Saturday" -> return 7
                else -> return 0
            }
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