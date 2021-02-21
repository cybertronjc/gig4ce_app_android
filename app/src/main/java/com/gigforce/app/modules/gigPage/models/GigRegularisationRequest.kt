package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.Timestamp

@Keep
class GigRegularisationRequest : BaseFirestoreDataModel(OBJECT_NAME) {

    var regularisationCompleted = false
    var regularisationSuccessful = false
    var successOrErrorMessage = ""
    var requestedOn : Timestamp? = null
    var completedOn : Timestamp? = null

    var checkInTime : Timestamp? = null
    var checkOutTime : Timestamp? = null

    companion object{
        const val OBJECT_NAME = "regularisation_request"
    }
}