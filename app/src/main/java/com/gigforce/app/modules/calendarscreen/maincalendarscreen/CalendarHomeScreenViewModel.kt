package com.gigforce.app.modules.calendarscreen.maincalendarscreen

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.AllotedGigDataModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.GigsDetail
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.MainHomeCompleteGigModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.VerticalCalendarDataItemModel
import com.gigforce.app.modules.custom_gig_preferences.CustomPreferencesDataModel
import com.gigforce.app.modules.custom_gig_preferences.UnavailableDataModel
import com.gigforce.app.modules.roster.models.Gig
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ServerTimestamp
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
        mainHomeRepository.getCollectionReference()
            .whereEqualTo("gigerId", mainHomeRepository.getUID())
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (querySnapshot != null) {
                    querySnapshot.documents.forEach { t ->

                        Log.d("gig id : data", t.id.toString())
                        t.toObject(GigData::class.java)
                            ?.let { arrMainHomeDataModel?.add(AllotedGigDataModel.getGigData(it)) }
                    }
                    mainHomeLiveDataModel.postValue(
                        MainHomeCompleteGigModel()
                    )
                }
            }
//            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
//                if (e != null) {
//                    return@EventListener
//                }
//                var data: MainHomeCompleteGigModel? = value!!.toObject(
//                    MainHomeCompleteGigModel::class.java
//                )
//
////                arrMainHomeDataModel = data?.all_gigs
//                arrMainHomeDataModel = getAllGigData()
//                mainHomeLiveDataModel.postValue(
//                    data
//                )
//            })
    }

    class GigData {
        var duration: Int = 0
        var gigStatus: String = ""
        var gigerId: String = ""
        @ServerTimestamp
        lateinit var startDateTime: Date
        @ServerTimestamp
        lateinit var endDateTime: Date
        var title: String = ""

        constructor(
            duration: Int,
            gigStatus: String,
            gigerId: String,
            startDateTime: Date,
            endDateTime: Date,
            title: String
        ) {
            this.duration = duration
            this.gigStatus = gigStatus
            this.gigerId = gigerId
            this.startDateTime = startDateTime
            this.endDateTime = endDateTime
            this.title = title
        }

        constructor() {}
    }

    fun setGigData() {
        var arrGigs = ArrayList<GigData>()
        var gigData = GigData(
            3,
            "upcoming",
            "xyOpFBoUOIRyE9O5VHd2JKM6FZs2",
            getStartDateTime(2, 6, 2020, 9, 0, Calendar.AM),
            getStartDateTime(2, 6, 2020, 12, 0, Calendar.PM),
            "title1"
        )
        var gigData1 = GigData(
            3,
            "upcoming",
            "xyOpFBoUOIRyE9O5VHd2JKM6FZs2",
            getStartDateTime(3, 6, 2020, 12, 0, Calendar.PM),
            getStartDateTime(3, 6, 2020, 3, 0, Calendar.PM),
            "title2"
        )
        var gigData2 = GigData(
            3,
            "upcoming",
            "xyOpFBoUOIRyE9O5VHd2JKM6FZs2",
            getStartDateTime(4, 6, 2020, 2, 0, Calendar.PM),
            getStartDateTime(4, 6, 2020, 5, 0, Calendar.PM),
            "title3"
        )
        var gigData3 = GigData(
            3,
            "upcoming",
            "xyOpFBoUOIRyE9O5VHd2JKM6FZs2",
            getStartDateTime(5, 6, 2020, 3, 0, Calendar.PM),
            getStartDateTime(5, 6, 2020, 6, 0, Calendar.PM),
            "title4"
        )
        var gigData4 = GigData(
            3,
            "upcoming",
            "xyOpFBoUOIRyE9O5VHd2JKM6FZs2",
            getStartDateTime(6, 6, 2020, 10, 0, Calendar.AM),
            getStartDateTime(6, 6, 2020, 1, 0, Calendar.PM),
            "title5"
        )
        var gigData5 = GigData(
            3,
            "upcoming",
            "xyOpFBoUOIRyE9O5VHd2JKM6FZs2",
            getStartDateTime(7, 6, 2020, 8, 0, Calendar.AM),
            getStartDateTime(7, 6, 2020, 11, 0, Calendar.AM),
            "title6"
        )
        arrGigs.add(gigData)
        arrGigs.add(gigData1)
        arrGigs.add(gigData2)
        arrGigs.add(gigData3)
        arrGigs.add(gigData4)
        arrGigs.add(gigData5)
        GigRepository().setGigData(arrGigs)
    }

    class GigRepository : BaseFirestoreDBRepository() {
        var custguid: String = ""
        override fun getCollectionName(): String {
            return "Gigs"
        }

        override fun getCustomUid(): String? {
            return custguid
        }

        fun setGigData(arrGigs: ArrayList<GigData>) {
            arrGigs.forEachIndexed { index, gig ->
                custguid = (index * 54321).toString()
                getCustomDBCollection().set(gig)
            }
        }

    }

    private fun getStartDateTime(
        date: Int,
        month: Int,
        year: Int,
        hour: Int,
        minute: Int,
        am_pm: Int
    ): Date {
        var calendar = Calendar.getInstance()
        calendar.set(Calendar.DATE, date)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.HOUR, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.AM_PM, am_pm)
        return calendar.time
    }

    private fun getGigData(
        date: Int,
        month: Int,
        year: Int,
        title: String,
        gigDetails: ArrayList<GigsDetail>,
        available: Boolean
    ): AllotedGigDataModel {
        var data = AllotedGigDataModel()
        data.date = date
        data.month = month
        data.year = year
        data.title = title
        data.gigDetails = gigDetails
        data.available = available
        return data
    }

    private fun getGigDetailData(title: String, isCompleted: Boolean): ArrayList<GigsDetail> {
        var arrayListGigDetail = ArrayList<GigsDetail>()
        var data = GigsDetail()
        data.subTitle = title
        data.gigCompleted = isCompleted
        arrayListGigDetail.add(data)
        return arrayListGigDetail
    }

    private fun getAllGigData(): ArrayList<AllotedGigDataModel>? {
        var arrayList = ArrayList<AllotedGigDataModel>()
        arrayList.add(
            getGigData(
                30,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", false),
                true
            )
        )

        arrayList.add(
            getGigData(
                29,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", false),
                true
            )
        )

        arrayList.add(
            getGigData(
                28,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", false),
                true
            )
        )

        arrayList.add(
            getGigData(
                27,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", true),
                true
            )
        )

        arrayList.add(
            getGigData(
                26,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", false),
                true
            )
        )

        arrayList.add(
            getGigData(
                25,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", true),
                true
            )
        )
        arrayList.add(
            getGigData(
                24,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", true),
                true
            )
        )
        arrayList.add(
            getGigData(
                23,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", false),
                true
            )
        )

        arrayList.add(
            getGigData(
                22,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", false),
                true
            )
        )

        arrayList.add(
            getGigData(
                21,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", true),
                true
            )
        )

        arrayList.add(
            getGigData(
                20,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", false),
                true
            )
        )

        arrayList.add(
            getGigData(
                19,
                5,
                2020,
                "Retail Sales Executive",
                getGigDetailData("Retail Sales Executive", true),
                true
            )
        )


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
        var temp: Int = -1
        if (dataItem != null) {
            calendar.set(Calendar.YEAR, dataItem.year);
            calendar.set(Calendar.MONTH, dataItem.month);
            temp = calendar.get(Calendar.MONTH)
            calendar.set(Calendar.DATE, dataItem.date + 1)
        } else {
            temp = calendar.get(Calendar.MONTH)

        }
        for (x in 0..60) {
            if ((calendar.get(Calendar.MONTH) - temp) != 0) {
                if (isPreviousDay) {
                    var newcalendar: Calendar = Calendar.getInstance();
                    newcalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
                    newcalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
                    newcalendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
                    datalist.add(0, VerticalCalendarDataItemModel.getMonthObject(newcalendar))
                } else
                    datalist.add(VerticalCalendarDataItemModel.getMonthObject(calendar))

                temp = calendar.get(Calendar.MONTH)
            } else {
                var isGigFound = false;
                if (arrMainHomeDataModel != null)
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
                                var subTitle = ""
                                try {
                                    subTitle = data.gigDetails.get(0).subTitle
                                } catch (e: Exception) {
                                    subTitle = data.title
                                }
                                if (isPreviousDay) {
                                    datalist.add(
                                        0,
                                        VerticalCalendarDataItemModel.getDetailedObject(
                                            subTitle,
                                            countGigs,
                                            calendar,
                                            isPreviousDay, isToday(calendar),customPreferenceUnavailableData
                                        )
                                    )
                                } else {
                                    datalist.add(
                                        VerticalCalendarDataItemModel.getDetailedObject(
                                            subTitle,
                                            countGigs,
                                            calendar,
                                            isPreviousDay,
                                            isToday(calendar),customPreferenceUnavailableData
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
                                isToday(calendar),customPreferenceUnavailableData
                            )
                        )
                    else
                        datalist.add(
                            VerticalCalendarDataItemModel.getIfNoGigFoundObject(
                                calendar,
                                isPreviousDay,
                                isToday(calendar),customPreferenceUnavailableData
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
            ) == calendar.get(Calendar.DATE) && currentDateCalendar.get(Calendar.YEAR) == calendar.get(
                Calendar.YEAR
            )
        ) {
            return true
        }
        return false
    }
    var customPreferenceUnavailableData = ArrayList<UnavailableDataModel>()
    fun setCustomPreferenceData(customPreferenceData: CustomPreferencesDataModel) {
        customPreferenceUnavailableData = customPreferenceData.unavailable
    }
}
