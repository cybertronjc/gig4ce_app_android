package com.gigforce.app.modules.onboarding

import com.gigforce.app.R

abstract class OnIntroSlidesCompleted {

    fun getResourceToNavigateTo():Int {
        //return R.id.loginFragment
        // calling language fragment first
        //return R.id.action_homeFragment_to_languageSelectFragment // why this is not working?
        //return R.id.homeFragment
        return R.id.Login
    }

    abstract fun invoke()
}