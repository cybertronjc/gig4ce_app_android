package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForPreferencesModule(baseImplementation: BaseNavigationImpl){

    init {
        val moduleName: String = "preferences"
        baseImplementation.registerRoute("${moduleName}/weekDayFragment", R.id.weekDayFragment)
        baseImplementation.registerRoute("${moduleName}/weekEndFragment", R.id.weekEndFragment)
        baseImplementation.registerRoute("${moduleName}/earningFragment",R.id.earningFragment)
        baseImplementation.registerRoute("${moduleName}/currentAddressEditFragment",R.id.currentAddressEditFragment)
        baseImplementation.registerRoute("${moduleName}/preferredLocationFragment",R.id.preferredLocationFragment)
        baseImplementation.registerRoute("${moduleName}/permanentAddressEditFragment",R.id.permanentAddressEditFragment)
        baseImplementation.registerRoute("${moduleName}/currentAddressViewFragment",R.id.currentAddressViewFragment)
        baseImplementation.registerRoute("${moduleName}/arrountCurrentAddress",R.id.arrountCurrentAddress)
        baseImplementation.registerRoute("${moduleName}/permanentAddressViewFragment",R.id.permanentAddressViewFragment)
        baseImplementation.registerRoute("${moduleName}/settingFragment",R.id.settingFragment)
        baseImplementation.registerRoute("${moduleName}/locationFragment",R.id.locationFragment)
        baseImplementation.registerRoute("${moduleName}/languagePreferenceFragment",R.id.languagePreferenceFragment)
        baseImplementation.registerRoute("${moduleName}/dayTimeFragment",R.id.dayTimeFragment)
    }
}