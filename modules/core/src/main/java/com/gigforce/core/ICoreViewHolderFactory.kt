package com.gigforce.core

import android.content.Context

interface ICoreViewHolderFactory{
    fun getViewHolder(context: Context, viewType:Int):CoreViewHolder
}