package com.gigforce.app.modules.onboarding

import com.gigforce.app.R

abstract class OnVideoResumeCompleted {

        fun getResourceToNavigateTo():Int {
            return R.id.homeFragment
        }

        abstract fun invoke()
}