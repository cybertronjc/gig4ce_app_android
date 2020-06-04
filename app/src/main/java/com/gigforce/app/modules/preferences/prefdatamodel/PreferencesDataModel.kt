package com.gigforce.app.modules.preferences.prefdatamodel


import com.google.firebase.firestore.DocumentReference

class PreferencesDataModel{
    var category :String = "";
    var isweekdaysenabled:Boolean = false
    var selecteddays :ArrayList<String> = ArrayList<String>()
    var selectedslots:ArrayList<String> = ArrayList<String>()
    var isweekendenabled:Boolean = false
    var selectedweekends:ArrayList<String> = ArrayList<String>()
    var selectedweekendslots:ArrayList<String> = ArrayList<String>()
    var locations:ArrayList<DocumentReference> = ArrayList<DocumentReference>()
    var isWorkFromHome:Boolean = false
    var languageName : String = ""
    var languageCode : String = ""

}