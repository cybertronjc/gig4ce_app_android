package com.gigforce.app.modules.custom_gig_preferences

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class CustomPreferencesRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Custom_Preferences"
    }
}