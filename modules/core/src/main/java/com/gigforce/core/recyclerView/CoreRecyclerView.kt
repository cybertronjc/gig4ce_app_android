package com.gigforce.core.recyclerView

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.R

open class CoreRecyclerView(
    context: Context,
    attrs: AttributeSet
) : RecyclerView(context, attrs) {

    constructor(context: Context, attrs: AttributeSet, layoutManager: LayoutManager) : this(
        context,
        attrs
    ) {
        this.layoutManager = layoutManager
    }

    init {
        //todo: handle for horizontal as well
        this.layoutManager = LinearLayoutManager(context)
        this.setDefaultAdapter(context)
        attrs?.let {
            val styledAttributes =
                context.obtainStyledAttributes(it, R.styleable.CoreRecyclerView, 0, 0)
            val orientationValue =
                styledAttributes.getInt(R.styleable.CoreRecyclerView_android_orientation, 1)
            orientation = orientationValue
        }
    }

    var orientation: Int
        get() = orientation
        set(value) {
            layoutManager = LinearLayoutManager(
                context?.applicationContext,
                value,
                false
            )
        }

    open fun setDefaultAdapter(context: Context) {
        this.adapter = CoreRecyclerAdapter(context)
    }

    val coreAdapter: CoreRecyclerAdapter
        get() = this.adapter as CoreRecyclerAdapter

    var collection: List<Any>
        get() = this.coreAdapter.collection
        set(value) {
            this.coreAdapter.collection = value
        }
}