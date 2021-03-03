package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForVerificationModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName = "verification"
        baseImplementation.registerRoute("${moduleName}/main", R.id.gigerVerificationFragment)
        baseImplementation.registerRoute("${moduleName}/DLCA", R.id.fragment_upload_dl_cl_act)


    }
}