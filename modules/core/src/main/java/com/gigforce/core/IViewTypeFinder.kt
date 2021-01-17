package com.gigforce.core

import android.content.Context

interface IViewTypeFinder{
    fun getViewType(data:Any):Int
    fun getViewHolder(context: Context, viewType:Int):CoreViewHolder
}