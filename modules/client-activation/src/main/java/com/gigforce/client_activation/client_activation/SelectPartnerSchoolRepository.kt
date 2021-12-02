package com.gigforce.client_activation.client_activation

import com.gigforce.core.fb.BaseFirestoreDBRepository

class SelectPartnerSchoolRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "JP_Settings"
    }

}