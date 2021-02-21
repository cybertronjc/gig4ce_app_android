package com.gigforce.app.modules.client_activation

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class ApplicationClientActivationRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Work_Order_Dependencies"
    }

}