package com.gigforce.common_ui.viewmodels.custom_gig_preferences

import com.gigforce.core.fb.BaseFirestoreDBRepository

class CustomPreferencesRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Custom_Preferences"
    }
}