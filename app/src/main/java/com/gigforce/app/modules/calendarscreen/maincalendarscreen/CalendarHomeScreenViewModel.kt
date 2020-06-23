package com.gigforce.app.modules.calendarscreen.maincalendarscreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.AllotedGigDataModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.GigsDetail
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.MainHomeCompleteGigModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.VerticalCalendarDataItemModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import java.util.*
import kotlin.collections.ArrayList

class CalendarHomeScreenViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var mainHomeRepository = CalendarHomeRepository()
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
                var data : MainHomeCompleteGigModel? = value!!.toObject(
                    MainHomeCompleteGigModel::class.java)

//                arrMainHomeDataModel = data?.all_gigs
                arrMainHomeDataModel = getAllGigData()
                mainHomeLiveDataModel.postValue(
                    data
                )
            })
    }
    private fun getGigData(date:Int,month:Int,year:Int,title:String,gigDetails:ArrayList<GigsDetail>,available:Boolean):AllotedGigDataModel{
        var data = AllotedGigDataModel()
        data.date = date
        data.month= month
        data.year = year
        data.title = title
        data.gigDetails =  gigDetails
        data.available = available
        return data
    }
    private fun getGigDetailData(title: String,isCompleted:Boolean):ArrayList<GigsDetail>{
        var arrayListGigDetail = ArrayList<GigsDetail>()
        var data = GigsDetail()
        data.subTitle = title
        data.gigCompleted = isCompleted
        arrayListGigDetail.add(data)
        return arrayListGigDetail
    }
    private fun getAllGigData(): ArrayList<AllotedGigDataModel>? {
        var arrayList = ArrayList<AllotedGigDataModel>()
        arrayList.add(getGigData(30,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",false),true))

        arrayList.add(getGigData(29,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",false),true))

        arrayList.add(getGigData(28,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",false),true))

        arrayList.add(getGigData(27,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",true),true))

        arrayList.add(getGigData(26,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",false),true))

        arrayList.add(getGigData(25,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",true),true))
        arrayList.add(getGigData(24,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",true),true))
        arrayList.add(getGigData(23,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",false),true))

        arrayList.add(getGigData(22,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",false),true))

        arrayList.add(getGigData(21,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",true),true))

        arrayList.add(getGigData(20,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",false),true))

        arrayList.add(getGigData(19,5,2020,"Retail Sale executive",getGigDetailData("Retail Sale executive",true),true))

        return arrayList
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
        calendar.time
        var temp: Int = calendar.get(Calendar.MONTH)

        for (x in 0..60) {
            if ((calendar.get(Calendar.MONTH) - temp) != 0) {
                if (isPreviousDay) {
                    var newcalendar: Calendar = Calendar.getInstance();
                    newcalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    newcalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    newcalendar.set(Calendar.DATE, calendar.get(Calendar.DATE)+1)
                    datalist.add(0, VerticalCalendarDataItemModel.getMonthObject(newcalendar))
                }
                else
                    datalist.add(VerticalCalendarDataItemModel.getMonthObject(calendar))

                temp = calendar.get(Calendar.MONTH)
            } else {
                var isGigFound = false;
                if(arrMainHomeDataModel!=null)
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
                        break;
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


//    fun setDataModel(mainHomeDataModel1: ArrayList<AllotedGigDataModel>?) {
//        arrMainHomeDataModel = mainHomeDataModel1
//    }

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
