package com.gigforce.app.modules.preferences.prefdatamodel

import com.gigforce.app.modules.preferences.location.models.LocationPreferenceModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint

class PreferencesDataModel {
    var category :String = "";

    var isweekdaysenabled:Boolean = false
    var selecteddays :ArrayList<String> = ArrayList<String>()
    var selectedslots:ArrayList<String> = ArrayList<String>()
    var isweekendenabled:Boolean = false
    var locations:ArrayList<DocumentReference> = ArrayList<DocumentReference>()
    var isWorkFromHome:Boolean = false

}