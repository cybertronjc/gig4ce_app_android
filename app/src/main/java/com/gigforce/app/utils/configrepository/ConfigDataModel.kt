package com.gigforce.app.utils.configrepository

import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

class ConfigDataModel {
    var time_slots:ArrayList<TimeSlot> = ArrayList<TimeSlot>()
}

class TimeSlot {
    @ServerTimestamp
    val start_time_slot: Date? = null
    val end_time_slot:Date? = null
    val time_slot_id : Int = 0
    override fun toString(): String {
        return time_slot_id.toString()
    }
}
