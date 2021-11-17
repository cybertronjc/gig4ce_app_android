package com.gigforce.common_ui.repository.prefrepo

import com.gigforce.core.fb.BaseFirestoreDBRepository

class PreferencesRepository : BaseFirestoreDBRepository() {
    var COLLECTION_NAME = "Preferences"
    var WORKINGDAYS = "selecteddays"
    var WORKINGSLOTS = "selectedslots"
    var WEEKDAYS = "isweekdaysenabled"
    var WEEKEND = "isweekendenabled"
    var WEEKENDDAYS = "selectedweekends"
    var WEEKENDSLOTS = "selectedweekendslots"
    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

}