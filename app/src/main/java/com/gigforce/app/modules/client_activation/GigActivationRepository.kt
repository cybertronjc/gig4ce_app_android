package com.gigforce.app.modules.client_activation

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class GigActivationRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Work_Order_Dependencies"
    }


}