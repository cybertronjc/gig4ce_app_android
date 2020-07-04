package com.gigforce.app.modules.custom_gig_preferences

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*
import kotlin.collections.ArrayList

class CustomPreferencesDataModel {
    var unavailable = ArrayList<UnavailableDataModel>()
}

class UnavailableDataModel : BaseFirestoreDataModel{
    var day: Int = -1
    var month: Int = -1
    var year: Int = -1
    var timeSlots = ArrayList<TimeSlotsDataModel>()
    var isDayUnavailable: Boolean = false
    constructor():super("unavailable") {
    }
    constructor(date: Date):super("unavailable")  {
        this.day = date.day
        this.month = date.month
        this.year = date.year
    }
    constructor(date: Int,
                month: Int,
                year: Int):super("unavailable") {
        this.day = date
        this.month = month
        this.year = year
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
            if (unavailable.day == this.day && unavailable.month == this.month && unavailable.year == this.year) {
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