package com.gigforce.client_activation.client_activation

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class ApplicationClientActivationRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Work_Order_Dependencies"
    }

}