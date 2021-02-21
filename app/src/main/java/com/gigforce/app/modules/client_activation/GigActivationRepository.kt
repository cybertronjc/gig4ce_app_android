package com.gigforce.app.modules.client_activation

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class GigActivationRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "JP_Settings"
    }


}