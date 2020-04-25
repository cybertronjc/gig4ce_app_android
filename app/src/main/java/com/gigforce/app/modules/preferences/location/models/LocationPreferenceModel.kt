package com.gigforce.app.modules.preferences.location.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.DocumentReference

class LocationPreferenceModel: BaseFirestoreDataModel {
    var location: ArrayList<DocumentReference> = ArrayList<DocumentReference>()
    constructor(
        location: ArrayList<DocumentReference> = ArrayList<DocumentReference>()
    ):super("locations"){
        this.location=location
    }


}
