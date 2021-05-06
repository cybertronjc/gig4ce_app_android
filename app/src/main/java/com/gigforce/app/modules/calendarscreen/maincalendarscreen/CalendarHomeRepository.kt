package com.gigforce.app.modules.calendarscreen.maincalendarscreen

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class CalendarHomeRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String {
//        getDBCollection()
        return "Gigs"
    }
}

