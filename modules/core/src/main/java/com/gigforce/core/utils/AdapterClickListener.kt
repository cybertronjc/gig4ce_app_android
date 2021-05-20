package com.gigforce.core.utils

import android.view.View

interface AdapterClickListener<T> {
    fun onItemClick(view : View, obj : T, position : Int)
}