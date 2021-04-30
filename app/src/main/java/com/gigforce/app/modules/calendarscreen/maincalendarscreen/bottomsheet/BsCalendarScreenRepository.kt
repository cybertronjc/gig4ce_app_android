package com.gigforce.app.modules.calendarscreen.maincalendarscreen.bottomsheet

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class BsCalendarScreenRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String {
        return "Profiles"
    }

}