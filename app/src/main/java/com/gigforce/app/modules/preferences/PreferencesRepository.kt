package com.gigforce.app.modules.preferences

import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.utils.dbrepository.BaseFirestoreDBRepository

class PreferencesRepository : BaseFirestoreDBRepository() {
    var COLLECTION_NAME = "Preferences"
    var WORKINGDAYS = "selecteddays"
    var WORKINGSLOTS = "selectedslots"
    var WEEKDAYS = "isweekdaysenabled"
    var WEEKEND = "isweekendenabled"
    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

}