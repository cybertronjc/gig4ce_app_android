package com.gigforce.app.modules.onboarding

import com.gigforce.app.R

abstract class OnIntroSlidesCompleted {

    fun getResourceToNavigateTo():Int {
        //return R.id.loginFragment
        // calling language fragment first
        return R.id.action_homeFragment_to_languageSelectFragment
    }

    abstract fun invoke()
}