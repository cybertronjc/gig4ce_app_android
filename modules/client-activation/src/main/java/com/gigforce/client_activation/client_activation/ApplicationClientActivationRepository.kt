package com.gigforce.client_activation.client_activation

import com.gigforce.core.fb.BaseFirestoreDBRepository


class ApplicationClientActivationRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "Work_Order_Dependencies"
    }

}