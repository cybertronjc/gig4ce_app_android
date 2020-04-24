package com.gigforce.app.modules.homescreen.mainhome

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.homescreen.mainhome.verticalcalendar.VerticalCalendarDataItemModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import java.util.*
import kotlin.collections.ArrayList

class MainHomeScreenViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var mainHomeRepository = MainHomeRepository()
    var mainHomeLiveDataModel: MutableLiveData<MainHomeCompleteGigModel> =
        MutableLiveData<MainHomeCompleteGigModel>()
    var arrMainHomeDataModel: ArrayList<AllotedGigDataModel>? = ArrayList<AllotedGigDataModel>()
    var currentDateCalendar: Calendar = Calendar.getInstance();
    init {
        getAllData()
    }

    fun getAllData() {
        mainHomeRepository.getDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    return@EventListener
                }
                mainHomeLiveDataModel.postValue(
                    value!!.toObject(MainHomeCompleteGigModel::class.java)
                )
            })
    }

    fun getAllCalendarData(): ArrayList<VerticalCalendarDataItemModel> {
        var datalist: ArrayList<VerticalCalendarDataItemModel> =
            ArrayList<VerticalCalendarDataItemModel>()
        datalist.addAll(getVerticalCalendarData(null, true))
        datalist.addAll(getVerticalCalendarData(datalist.get(datalist.size - 1), false))
        return datalist
    }

    fun getVerticalCalendarData(
        dataItem: VerticalCalendarDataItemModel?,
        isPreviousDay: Boolean
    ): ArrayList<VerticalCalendarDataItemModel> {
        var datalist: ArrayList<VerticalCalendarDataItemModel> =
            ArrayList<VerticalCalendarDataItemModel>()
        var calendar: Calendar = Calendar.getInstance();
        if (dataItem != null) {
            calendar.set(Calendar.YEAR, dataItem.year);
            calendar.set(Calendar.MONTH, dataItem.month);
            calendar.set(Calendar.DATE, dataItem.date + 1)
        }
        var temp: Int = calendar.get(Calendar.MONTH)

        for (x in 0..20) {
            if ((calendar.get(Calendar.MONTH) - temp) != 0) {
                if (isPreviousDay)
                    datalist.add(0, VerticalCalendarDataItemModel.getMonthObject(calendar))
                else
                    datalist.add(VerticalCalendarDataItemModel.getMonthObject(calendar))

                temp = calendar.get(Calendar.MONTH)
            } else {
                var isGigFound = false;
                for (data in arrMainHomeDataModel!!) {
                    if (data.month == calendar.get(Calendar.MONTH) && data.date == calendar.get(
                            Calendar.DATE
                        )
                    ) {
                        if (data.gigDetails != null) {
                            var countGigs = ""
                            if (data.gigDetails.size > 1) {
                                countGigs = "+" + (data.gigDetails.size - 1) + " More"
                            }
                            var subTitle = data.gigDetails.get(0).subTitle
                            if (isPreviousDay) {
                                datalist.add(
                                    0,
                                    VerticalCalendarDataItemModel.getDetailedObject(
                                        subTitle,
                                        countGigs,
                                        calendar,
                                        isPreviousDay, isToday(calendar)
                                    )
                                )
                            } else {
                                datalist.add(
                                    VerticalCalendarDataItemModel.getDetailedObject(
                                        subTitle,
                                        countGigs,
                                        calendar,
                                        isPreviousDay,
                                        isToday(calendar)
                                    )
                                )
                            }
                        }
                        isGigFound = true
                    }
                }
                if (!isGigFound) {
                    if (isPreviousDay)
                        datalist.add(
                            0,
                            VerticalCalendarDataItemModel.getIfNoGigFoundObject(
                                calendar,
                                isPreviousDay,
                                isToday(calendar)
                            )
                        )
                    else
                        datalist.add(
                            VerticalCalendarDataItemModel.getIfNoGigFoundObject(
                                calendar,
                                isPreviousDay,
                                isToday(calendar)
                            )
                        )
                }

                temp = calendar.get(Calendar.MONTH)
                var newDate = calendar.get(Calendar.DATE);
                if (isPreviousDay) {
                    newDate -= 1
                } else {
                    newDate += 1
                }
                calendar.set(Calendar.DATE, newDate)
            }
        }
        return datalist
    }


    fun setDataModel(mainHomeDataModel1: ArrayList<AllotedGigDataModel>?) {
        arrMainHomeDataModel = mainHomeDataModel1
    }

    fun isToday(calendar: Calendar): Boolean {

        if (currentDateCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && currentDateCalendar.get(
                Calendar.DATE
            ) == calendar.get(Calendar.DATE)&&currentDateCalendar.get(Calendar.YEAR)==calendar.get(Calendar.YEAR)
        ) {
            return true
        }
        return false
    }
}


//if (x % 6 == 0) {
//    datalist.add(
//        0,
//        VerticalCalendarDataItemModel(
//            "Swiggy Deliveries",
//            "+3 More",
//            getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//            false,
//            true,
//            false,
//            false,
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DATE),
//            ""
//        )
//    )
//}
//else if (x % 6 == 1) {
//    datalist.add(0,
//        VerticalCalendarDataItemModel(
//            "Swiggy Deliveries",
//            "+3 More",
//            getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//            true,
//            true,
//            false,
//            false,
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DATE),
//            ""
//        )
//    )
//} else if (x % 6 == 2) {
//    datalist.add(0,
//        VerticalCalendarDataItemModel(
//            "Swiggy Deliveries",
//            "+3 More",
//            getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//            false,
//            false,
//            false,
//            false,
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DATE),
//            ""
//        )
//    )
//} else {
//    datalist.add(0,
//        VerticalCalendarDataItemModel(
//            "Swiggy Deliveries",
//            "+3 More",
//            getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//            false,
//            false,
//            true,
//            false,
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DATE),
//            ""
//        )
//    )
//}


//data will link to DB
//fun getNextDaysVerticalCalendarData(dataItem: VerticalCalendarDataItemModel?): ArrayList<VerticalCalendarDataItemModel> {
//    var datalist: ArrayList<VerticalCalendarDataItemModel> = ArrayList<VerticalCalendarDataItemModel>()
//
//    var calendar : Calendar = Calendar.getInstance();
//    if(dataItem!=null){
//        calendar.set(Calendar.YEAR,dataItem.year);
//        calendar.set(Calendar.MONTH,dataItem.month);
//        calendar.set(Calendar.DATE,dataItem.date+1)
//    }
//    var temp:Int = calendar.get(Calendar.MONTH)
//    for (x in 0..20){
//        if((calendar.get(Calendar.MONTH)-temp)!=0){
//            datalist.add(
//                getMonthObject(calendar)
//            )
//            temp = calendar.get(Calendar.MONTH)
//
//        }else {
//            var isGigFound = false;
//            for(data in arrMainHomeDataModel!!){
//                if(data.month==calendar.get(Calendar.MONTH)&&data.date==calendar.get(Calendar.DATE)){
//                    if(data.gigDetails!=null) {
//                        var countGigs = ""
//                        if(data.gigDetails.size>1){
//                            countGigs = "+"+data.gigDetails.size+" More"
//                        }
//
//                        datalist.add(
//                            VerticalCalendarDataItemModel(
//                                data.gigDetails.get(0).subTitle,
//                                countGigs,
//                                getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//                                false,
//                                true,
//                                true,
//                                false,
//                                calendar.get(Calendar.YEAR),
//                                calendar.get(Calendar.MONTH),
//                                calendar.get(Calendar.DATE),
//                                ""
//                            )
//                        )
//                    }
//                    isGigFound = true
//                }
//            }
//            if(!isGigFound){
//                datalist.add(
//                    VerticalCalendarDataItemModel(
//                        "",
//                        "",
//                        getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//                        false,
//                        true,
//                        false,
//                        false,
//                        calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DATE),
//                        ""
//                    )
//                )
//            }
//
//            if (x % 6 == 0) {
//                datalist.add(
//                    VerticalCalendarDataItemModel(
//                        "Swiggy Deliveries",
//                        "+3 More",
//                        getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//                        false,
//                        true,
//                        false,
//                        false,
//                        calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DATE),
//                        ""
//                    )
//                )
//            }
//            else if (x % 6 == 1) {
//                datalist.add(
//                    VerticalCalendarDataItemModel(
//                        "Swiggy Deliveries",
//                        "+3 More",
//                        getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//                        true,
//                        true,
//                        false,
//                        false,
//                        calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DATE),
//                        ""
//                    )
//                )
//            } else if (x % 6 == 2) {
//                datalist.add(
//                    VerticalCalendarDataItemModel(
//                        "Swiggy Deliveries",
//                        "+3 More",
//                        getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//                        false,
//                        false,
//                        false,
//                        false,
//                        calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DATE),
//                        ""
//                    )
//                )
//            } else {
//                datalist.add(
//                    VerticalCalendarDataItemModel(
//                        "Swiggy Deliveries",
//                        "+3 More",
//                        getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),
//                        false,
//                        false,
//                        true,
//                        false,
//                        calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DATE),
//                        ""
//                    )
//                )
//            }
//            temp = calendar.get(Calendar.MONTH)
//            calendar.set(Calendar.DATE,calendar.get(Calendar.DATE)+1)
//        }
//
//    }
//    return datalist
//}

//        datalist.addAll(getNextDaysVerticalCalendarData(datalist.get(datalist.size-1)))
