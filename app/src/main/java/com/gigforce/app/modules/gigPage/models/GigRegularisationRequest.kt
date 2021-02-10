package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

@Keep
class GigRegularisationRequest : BaseFirestoreDataModel(OBJECT_NAME) {

    @get:PropertyName("contactPersons")
    @set:PropertyName("contactPersons")
    var requestedOn: Timestamp? = null

    @get:PropertyName("regularisationSettled")
    @set:PropertyName("regularisationSettled")
    var regularisationSettled = false

    @get:PropertyName("checkInTimeAccToUser")
    @set:PropertyName("checkInTimeAccToUser")
    var checkInTimeAccToUser: Timestamp? = null

    @get:PropertyName("checkOutTimeAccToUser")
    @set:PropertyName("checkOutTimeAccToUser")
    var checkOutTimeAccToUser: Timestamp? = null

    @get:PropertyName("remarksFromUser")
    @set:PropertyName("remarksFromUser")
    var remarksFromUser = ""

    @get:PropertyName("remarksFromManager")
    @set:PropertyName("remarksFromManager")
    var remarksFromManager = ""

    companion object {
        const val OBJECT_NAME = "regularisation_request"
    }
}