package com.gigforce.core.datamodels.custom_gig_preferences

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*
import kotlin.collections.ArrayList

class CustomPreferencesDataModel {
    var unavailable = ArrayList<UnavailableDataModel>()
}

class UnavailableDataModel : BaseFirestoreDataModel{
    lateinit var date :Date
    var timeSlots = ArrayList<TimeSlotsDataModel>()
    var dayUnavailable: Boolean = false
    constructor():super("unavailable") {
    }
    constructor(date: Date):super("unavailable")  {
        this.date = date
    }
    constructor(sDateTime: Date, eDateTime:Date):super("unavailable")  {
        this.date = sDateTime
        this.timeSlots.add(
            TimeSlotsDataModel(
                sDateTime,
                eDateTime
            )
        )
    }
    fun findDateDataModel(
        arrUnavailable: ArrayList<UnavailableDataModel>
    ): UnavailableDataModel? {
        return findDataModel(arrUnavailable)
    }

    private fun findDataModel(
        arrUnavailable: java.util.ArrayList<UnavailableDataModel>
    ): UnavailableDataModel? {
        for (unavailable in arrUnavailable) {
            if (unavailable.date.date == this.date.date && unavailable.date.month == this.date.month && unavailable.date.year == this.date.year) {
                return unavailable
            }
        }
        return null
    }


    fun findDataModelForSlot(
        arrUnavailable: java.util.ArrayList<UnavailableDataModel>
    ): UnavailableDataModel? {
        for (unavailable in arrUnavailable) {
            if (unavailable.date.date == this.date.date && unavailable.date.month == this.date.month && unavailable.date.year == this.date.year) {
                return unavailable
            }
        }
        return null
    }

    fun copyObject(): UnavailableDataModel {
        var obj =
            UnavailableDataModel(
                date
            )
        obj.timeSlots = ArrayList<TimeSlotsDataModel>()
        obj.timeSlots.addAll(timeSlots)
        return obj
    }

    fun setAvailaleSlots(unavailableDataModel: UnavailableDataModel){
        timeSlots.forEachIndexed { index, timeSlotsDataModel ->
            if(isTimeEqual(timeSlotsDataModel.startTime,unavailableDataModel.timeSlots.get(0).startTime) && isTimeEqual(timeSlotsDataModel.endTime,unavailableDataModel.timeSlots.get(0).endTime)) {
                timeSlots.removeAt(index)

            }
        }
    }

    private fun isTimeEqual(startTime: Date, startTime1: Date): Boolean {
        if(startTime.hours == startTime1.hours && startTime.minutes == startTime1.minutes){
            return true
        }
        return false
    }

    fun setUnavailaleSlots(unavailableDataModel: UnavailableDataModel) {
        timeSlots.add(
            TimeSlotsDataModel(
                unavailableDataModel.timeSlots.get(0).startTime,
                unavailableDataModel.timeSlots.get(0).endTime
            )
        )
    }


}

class TimeSlotsDataModel {
    constructor(sDateTime: Date, eDateTime:Date){
        this.startTime = sDateTime
        this.endTime = eDateTime
    }
    constructor(){}
    lateinit var startTime: Date
    lateinit var endTime: Date
}