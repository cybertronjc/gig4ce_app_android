package com.gigforce.app.modules.roster

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import com.gigforce.app.core.base.BaseFragment


abstract class RosterBaseFragment: BaseFragment() {

    open fun setMargins(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        if (view.getLayoutParams() is MarginLayoutParams) {
            val p = view.getLayoutParams() as MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }
}