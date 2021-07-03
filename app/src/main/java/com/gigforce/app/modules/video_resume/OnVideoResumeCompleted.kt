package com.gigforce.app.modules.video_resume

import com.gigforce.app.R

abstract class OnVideoResumeCompleted {

        fun getResourceToNavigateTo():Int {
            return -1//R.id.homeFragment
        }

        abstract fun invoke()
}