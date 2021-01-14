package com.gigforce.core

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

class CoreViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    init {
        if(view !is IViewHolder){
            throw Exception("View must extend IViewHolder");
        }
    }

    val IViewHolder:IViewHolder
        get() {return view as IViewHolder;}
}