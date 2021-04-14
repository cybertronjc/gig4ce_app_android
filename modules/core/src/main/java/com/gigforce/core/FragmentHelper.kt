package com.gigforce.core

import android.app.Activity
import androidx.fragment.app.Fragment

object FragmentHelper {

    fun isFragmentVisible(fragment: Fragment): Boolean {
        val activity: Activity? = fragment.activity
        val focusedView = fragment.requireView().findFocus()
        return activity != null && focusedView != null && focusedView === activity.window.decorView.findFocus()
    }
}