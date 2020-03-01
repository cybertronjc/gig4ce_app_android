package com.gigforce.app.modules.onboarding

import com.gigforce.app.R

abstract class OnIntroSlidesCompleted {

    fun getResourceToNavigateTo():Int {
        return R.id.loginFragment
    }

    abstract fun invoke()
}