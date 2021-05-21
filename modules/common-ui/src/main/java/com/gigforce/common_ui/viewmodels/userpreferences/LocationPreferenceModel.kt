package com.gigforce.common_ui.viewmodels.userpreferences

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.DocumentReference

class LocationPreferenceModel: BaseFirestoreDataModel {
    var location: ArrayList<DocumentReference> = ArrayList<DocumentReference>()
    constructor(
        location: ArrayList<DocumentReference> = ArrayList<DocumentReference>()
    ):super("locations"){
        this.location=location
    }


}
