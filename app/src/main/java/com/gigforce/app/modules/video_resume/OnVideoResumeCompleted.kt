package com.gigforce.app.modules.video_resume

import com.gigforce.app.R

abstract class OnVideoResumeCompleted {

        fun getResourceToNavigateTo():Int {
            return R.id.homeFragment
        }

        abstract fun invoke()
}