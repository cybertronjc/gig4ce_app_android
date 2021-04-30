package com.gigforce.app.modules.onboarding

import com.gigforce.app.R

abstract class OnOBSlidesCompleted {

    fun getResourceToNavigateTo():Int {
        return R.id.createInitProfile
    }

    abstract fun invoke()
}
