package com.gigforce.app.modules.client_activation

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class ClientActivationRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Job_Profiles"
    }





}