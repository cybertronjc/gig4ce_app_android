package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForLearningModule(
    baseImplementation: BaseNavigationImpl){

    init {
        val moduleName:String = "learning"

        baseImplementation.registerRoute("${moduleName}/main", R.id.mainLearningFragment)
        baseImplementation.registerRoute("${moduleName}/course", R.id.mainLearningFragment)
        baseImplementation.registerRoute("${moduleName}/lesson", R.id.mainLearningFragment)
        baseImplementation.registerRoute("${moduleName}/assessment", R.id.mainLearningFragment)
    }
}