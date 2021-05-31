package com.gigforce.giger_app.calendarscreen.maincalendarscreen.bottomsheet

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class BsCalendarScreenRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String {
        return "Profiles"
    }

}