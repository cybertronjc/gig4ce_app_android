package com.gigforce.core

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class CoreRecyclerView(context: Context,
                       attrs: AttributeSet): RecyclerView(context, attrs) {

    constructor(context: Context, attrs: AttributeSet, layoutManager: LayoutManager): this(context, attrs){
        this.layoutManager = layoutManager
    }

    init {
        this.layoutManager = LinearLayoutManager(context)
    }

    val coreAdapter: CoreRecyclerAdapter
        get() = this.adapter as CoreRecyclerAdapter

    var collection:List<Any>
        get() = this.coreAdapter.collection
        set(value) {this.coreAdapter.collection = value}

    fun <T: View> setDefaultAdapter(collection: List<Any>, factory: (context:Context)->T)
        :CoreRecyclerAdapter
    {
        this.adapter = CoreRecyclerAdapter.default(context, collection, factory)
        return this.adapter as CoreRecyclerAdapter
    }
}