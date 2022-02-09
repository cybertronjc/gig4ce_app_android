package com.gigforce.core

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

open class CoreViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    companion object{
        fun <T:View> default(context: Context, factory: (context:Context)->T): (parent: ViewGroup, viewType: Int) -> CoreViewHolder {
            fun returnFn(parent: ViewGroup, viewType: Int): CoreViewHolder{
                return CoreViewHolder(factory(context))
            }
            return ::returnFn
        }
    }

    init {
        // view passed must implement IViewHolder
        if(view !is IViewHolder){
            throw Exception("View must extend IViewHolder");
        }
    }

    val IViewHolder:IViewHolder
        get() {return view as IViewHolder;}

    val IItemClickListener : ICustomClickListener?
        get() {return view as? ICustomClickListener
        }
}