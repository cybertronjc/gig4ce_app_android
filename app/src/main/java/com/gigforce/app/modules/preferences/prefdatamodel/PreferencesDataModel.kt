package com.gigforce.app.modules.preferences.prefdatamodel

import android.location.Location

class PreferencesDataModel {
    var category :String = "";

    var isweekdaysenabled:Boolean = false
    var selecteddays :ArrayList<String> = ArrayList<String>()
    var selectedslots:ArrayList<String> = ArrayList<String>()
    var isweekendenabled:Boolean = false
    var locations:ArrayList<Location> = ArrayList<Location>()

}