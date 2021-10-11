package com.gigforce.core.recyclerView

import android.view.View

interface ItemClickListener {
    fun onItemClick(view: View,position:Int,dataModel:Any)
}

