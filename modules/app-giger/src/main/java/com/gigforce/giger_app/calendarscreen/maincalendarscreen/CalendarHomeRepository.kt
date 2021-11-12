package com.gigforce.giger_app.calendarscreen.maincalendarscreen

import com.gigforce.core.fb.BaseFirestoreDBRepository

class CalendarHomeRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String {
//        getDBCollection()
        return "Gigs"
    }
}

