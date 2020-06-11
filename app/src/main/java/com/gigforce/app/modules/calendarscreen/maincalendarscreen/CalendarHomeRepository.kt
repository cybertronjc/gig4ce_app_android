package com.gigforce.app.modules.calendarscreen.maincalendarscreen

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class CalendarHomeRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String {
//        getDBCollection()
        return "alloted_gigs_vol2"

    }
}

