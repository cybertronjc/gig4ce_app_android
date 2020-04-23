package com.gigforce.app.modules.homescreen.mainhome

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.homescreen.mainhome.verticalcalendar.VerticalCalendarDataItemModel
import java.util.*
import kotlin.collections.ArrayList

class MainHomeScreenViewModel : ViewModel() {
    // TODO: Implement the ViewModel


    //data will link to DB
    fun getVerticalCalendarData(dataItem: VerticalCalendarDataItemModel?): ArrayList<VerticalCalendarDataItemModel> {
        var datalist: ArrayList<VerticalCalendarDataItemModel> = ArrayList<VerticalCalendarDataItemModel>()

        var calendar : Calendar = Calendar.getInstance();
        if(dataItem!=null){
            calendar.set(Calendar.YEAR,dataItem.year);
            calendar.set(Calendar.MONTH,dataItem.month);
            calendar.set(Calendar.DATE,dataItem.date+1)
        }
        var temp:Int = calendar.get(Calendar.MONTH)
        for (x in 0..30){
            if((calendar.get(Calendar.MONTH)-temp)!=0){
                datalist.add(
                    VerticalCalendarDataItemModel(
                        "Swiggy Deliveries",
                        "+3 More",
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
                )
                temp = calendar.get(Calendar.MONTH)

            }else {
                if (x % 6 == 0)
                    datalist.add(
                        VerticalCalendarDataItemModel(
                            "Swiggy Deliveries",
                            "+3 More",
                            getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                            false,
                            true,
                            false,
                            false,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DATE),
                            ""
                        )
                    )
                else if (x % 6 == 1) {
                    datalist.add(
                        VerticalCalendarDataItemModel(
                            "Swiggy Deliveries",
                            "+3 More",
                            getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                            true,
                            true,
                            false,
                            false,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DATE),
                            ""
                        )
                    )
                } else if (x % 6 == 2) {
                    datalist.add(
                        VerticalCalendarDataItemModel(
                            "Swiggy Deliveries",
                            "+3 More",
                            getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                            false,
                            false,
                            false,
                            false,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DATE),
                            ""
                        )
                    )
                } else {
                    datalist.add(
                        VerticalCalendarDataItemModel(
                            "Swiggy Deliveries",
                            "+3 More",
                            getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
                            false,
                            false,
                            true,
                            false,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DATE),
                            ""
                        )
                    )
                }
            temp = calendar.get(Calendar.MONTH)
            calendar.set(Calendar.DATE,calendar.get(Calendar.DATE)+1)
            }

        }
        return datalist
    }
    private fun getMonth(month: Int): String {
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
    private fun getDayOfWeek(dayofweek: Int): String {
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