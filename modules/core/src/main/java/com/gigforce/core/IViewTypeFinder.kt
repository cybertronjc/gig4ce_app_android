package com.gigforce.core

import android.content.Context

interface IViewTypeFinder{
    fun getViewHolder(context: Context, viewType:Int):CoreViewHolder
}