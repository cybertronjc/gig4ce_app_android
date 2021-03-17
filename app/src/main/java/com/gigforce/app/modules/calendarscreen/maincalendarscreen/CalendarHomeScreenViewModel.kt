package com.gigforce.app.modules.calendarscreen.maincalendarscreen

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.AllotedGigDataModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.GigsDetail
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.MainHomeCompleteGigModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.VerticalCalendarDataItemModel
import com.gigforce.app.modules.custom_gig_preferences.CustomPreferencesDataModel
import com.gigforce.app.modules.custom_gig_preferences.UnavailableDataModel
import com.gigforce.app.modules.gigPage.models.JobProfile
import com.gigforce.app.modules.preferences.PreferencesRepository
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.riningan.widget.ExtendedBottomSheetBehavior
import java.util.*
import kotlin.collections.ArrayList

class CalendarHomeScreenViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var currentBottomSheetState = ExtendedBottomSheetBehavior.STATE_COLLAPSED


    var mainHomeRepository = CalendarHomeRepository()
    var mainHomeLiveDataModel: MutableLiveData<MainHomeCompleteGigModel> =
            MutableLiveData<MainHomeCompleteGigModel>()
    var arrMainHomeDataModel: ArrayList<AllotedGigDataModel>? = ArrayList<AllotedGigDataModel>()
    var currentDateCalendar: Calendar = Calendar.getInstance()
    var preferencesRepository: PreferencesRepository = PreferencesRepository()
    var preferenceDataModel: MutableLiveData<PreferencesDataModel> =
            MutableLiveData<PreferencesDataModel>()

    init {
        getAllData()
    }

    fun getAllData() {
        mainHomeRepository.getCollectionReference()
                .whereEqualTo("gigerId", mainHomeRepository.getUID())
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (arrMainHomeDataModel != null) {
                        arrMainHomeDataModel?.clear()
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
                }
        preferencesRepository.getDBCollection()
                .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                    if (e != null) {
                        return@EventListener
                    }
                    if (value?.data != null) {
                        preferenceDataModel.postValue(
                                value.toObject(PreferencesDataModel::class.java)
                        )
                    }
                })
    }


    var customPreferenceUnavailableData = ArrayList<UnavailableDataModel>()
    fun setCustomPreferenceData(customPreferenceData: CustomPreferencesDataModel) {
        customPreferenceUnavailableData = customPreferenceData.unavailable
    }

    var preferenceData: PreferencesDataModel? = null
    fun setPreferenceDataModel(preferenceData: PreferencesDataModel?) {
        this.preferenceData = preferenceData
    }

    fun getAllCalendarData(): ArrayList<VerticalCalendarDataItemModel> {
        var datalist: ArrayList<VerticalCalendarDataItemModel> =
                ArrayList<VerticalCalendarDataItemModel>()
        datalist.addAll(getVerticalCalendarData(null, true))
        datalist.addAll(getVerticalCalendarData(datalist.get(datalist.size - 1), false))
        return datalist
    }

    class GigData {
        var duration: Int = 0
        var gigStatus: String = ""
        var gigerId: String = ""

        @ServerTimestamp
        lateinit var startDateTime: Date

        @ServerTimestamp
        var endDateTime: Date? = null

        @get:PropertyName("title")
        @set:PropertyName("title")
        var title: String = ""

        @get:PropertyName("profile")
        @set:PropertyName("profile")
        var profile: JobProfile? = JobProfile()


        constructor(
                duration: Int,
                gigStatus: String,
                gigerId: String,
                startDateTime: Date,
                endDateTime: Date?,
                profile: JobProfile?,
                legacyTitle: String?
        ) {
            this.duration = duration
            this.gigStatus = gigStatus
            this.gigerId = gigerId
            this.startDateTime = startDateTime
            this.endDateTime = endDateTime
            this.profile = profile
            this.title = legacyTitle ?: ""
        }

        constructor()

        @Exclude
        fun getGigTitle(): String {
            return profile?.title ?: title
        }

    }


    fun getVerticalCalendarData(
            dataItem: VerticalCalendarDataItemModel?,
            isPreviousDay: Boolean
    ): ArrayList<VerticalCalendarDataItemModel> {
        var datalist: ArrayList<VerticalCalendarDataItemModel> =
                ArrayList<VerticalCalendarDataItemModel>()
        var calendar: Calendar = Calendar.getInstance()
        var temp: Int = -1
        if (dataItem != null) {
            calendar.set(Calendar.YEAR, dataItem.year)
            calendar.set(Calendar.MONTH, dataItem.month)
            temp = calendar.get(Calendar.MONTH)
            calendar.set(Calendar.DATE, dataItem.date + 1)
        } else {
            temp = calendar.get(Calendar.MONTH)
        }
        for (x in 0..180) {
            if ((calendar.get(Calendar.MONTH) - temp) != 0) {
                if (isPreviousDay) {
                    var newcalendar: Calendar = Calendar.getInstance()
                    newcalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                    newcalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                    newcalendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1)
                    datalist.add(0, VerticalCalendarDataItemModel.getMonthObject(newcalendar))
                } else
                    datalist.add(VerticalCalendarDataItemModel.getMonthObject(calendar))

                temp = calendar.get(Calendar.MONTH)
            } else {
                var isGigFound = false
                var verticalCalendarData: VerticalCalendarDataItemModel? = null
                var count = -1
                if (arrMainHomeDataModel != null)
                    for (data in arrMainHomeDataModel!!) {
                        if (data.month == calendar.get(Calendar.MONTH) && data.date == calendar.get(
                                        Calendar.DATE
                                )
                        ) {
                            count++
                            if (verticalCalendarData != null) {
                                verticalCalendarData.subTitle = "+" + count + " More"
                                verticalCalendarData.gigCount = count + 1
                            } else {
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
                                    verticalCalendarData = VerticalCalendarDataItemModel.getDetailedObject(
                                            subTitle,
                                            countGigs,
                                            calendar,
                                            isPreviousDay, isToday(calendar), customPreferenceUnavailableData, preferenceData
                                    )
                                    datalist.add(
                                            0,
                                            verticalCalendarData
                                    )
                                } else {
                                    verticalCalendarData = VerticalCalendarDataItemModel.getDetailedObject(
                                            subTitle,
                                            countGigs,
                                            calendar,
                                            isPreviousDay,
                                            isToday(calendar), customPreferenceUnavailableData, preferenceData
                                    )
                                    datalist.add(
                                            verticalCalendarData
                                    )
                                }
                            }
                            isGigFound = true

                        }
                    }
                if (!isGigFound) {
                    if (isPreviousDay) {
                        verticalCalendarData = VerticalCalendarDataItemModel.getIfNoGigFoundObject(
                                calendar,
                                isPreviousDay,
                                isToday(calendar), customPreferenceUnavailableData, preferenceData
                        )
                        datalist.add(
                                0,
                                verticalCalendarData
                        )
                    } else {
                        verticalCalendarData = VerticalCalendarDataItemModel.getIfNoGigFoundObject(
                                calendar,
                                isPreviousDay,
                                isToday(calendar), customPreferenceUnavailableData, preferenceData
                        )
                        datalist.add(
                                verticalCalendarData
                        )
                    }

                }

                temp = calendar.get(Calendar.MONTH)
                var newDate = calendar.get(Calendar.DATE)
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


    //Below code will be removed later
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
