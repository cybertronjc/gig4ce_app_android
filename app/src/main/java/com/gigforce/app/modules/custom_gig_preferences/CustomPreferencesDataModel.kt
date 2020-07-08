package com.gigforce.app.modules.custom_gig_preferences

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*
import kotlin.collections.ArrayList

class CustomPreferencesDataModel {
    var unavailable = ArrayList<UnavailableDataModel>()
}

class UnavailableDataModel : BaseFirestoreDataModel{
    lateinit var date :Date
    var timeSlots = ArrayList<TimeSlotsDataModel>()
    var isDayUnavailable: Boolean = false
    constructor():super("unavailable") {
    }
    constructor(date: Date):super("unavailable")  {
        this.date = date
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
}

class TimeSlotsDataModel {
    lateinit var startTime: Date
    lateinit var endTime: Date
}