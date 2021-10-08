package com.gigforce.core.recyclerView

import android.view.View

interface ItemLongClickListener {
    fun onItemLongClick(view: View, position: Int, dataModel: Any) : Boolean?
}