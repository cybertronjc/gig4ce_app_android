package com.gigforce.app.modules.preferences.prefdatamodel

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

class PreferencesDataModel{
    var category :String = "";

    var isweekdaysenabled:Boolean = false
    var selecteddays :ArrayList<String> = ArrayList<String>()
    var selectedslots:ArrayList<String> = ArrayList<String>()
    var isweekendenabled:Boolean = false
}