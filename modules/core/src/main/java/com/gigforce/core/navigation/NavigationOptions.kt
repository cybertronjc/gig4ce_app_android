package com.gigforce.core.navigation

import androidx.navigation.navOptions
import com.gigforce.core.R

object NavigationOptions {

    fun getNavOptions() = navOptions {
        this.anim {
            this.enter = R.anim.anim_enter_from_right
            this.exit = R.anim.anim_exit_to_left
            this.popEnter = R.anim.anim_enter_from_left
            this.popExit = R.anim.anim_exit_to_right
        }
    }
}