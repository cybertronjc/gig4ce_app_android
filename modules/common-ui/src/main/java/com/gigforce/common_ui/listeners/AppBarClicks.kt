package com.gigforce.common_ui.listeners

import android.view.View

object AppBarClicks {

    interface OnSearchClickListener{
        fun onSearchClick(v: View)
    }

    interface OnMenuClickListener{
        fun onMenuClick(v: View)
    }

}