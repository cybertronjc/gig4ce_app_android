package com.gigforce.app.modules.client_activation

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class ConfirmationDialogDrivingTestRepository() : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "JP_Applications"
    }

}