package com.gigforce.core.recyclerView

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.ICoreViewHolderFactory
import com.gigforce.core.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
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

    @Inject
    lateinit var iViewTypeFinder: ICoreViewHolderFactory

    init {
        //todo: handle for horizontal as well
        this.layoutManager = LinearLayoutManager(context)
        this.setDefaultAdapter(context)
        attrs.let {
            val styledAttributes =
                context.obtainStyledAttributes(it, R.styleable.CoreRecyclerView, 0, 0)
            val orientationValue =
                styledAttributes.getInt(R.styleable.CoreRecyclerView_android_orientation, 0)
            val noOfRows =
                styledAttributes.getInt(R.styleable.CoreRecyclerView_rows, 1)
            setOrientationAndRows(orientationValue, noOfRows)
        }
    }

    fun setOrientationAndRows(orientation: Int, noOfRows: Int) {
        layoutManager = GridLayoutManager(
            context?.applicationContext,
            noOfRows,orientation,
            false
        )

    }
    open fun setDefaultAdapter(context: Context) {
        this.adapter = CoreRecyclerAdapter(context, iViewTypeFinder)
    }

    val coreAdapter: CoreRecyclerAdapter
        get() = this.adapter as CoreRecyclerAdapter

    var collection: List<Any>
        get() = this.coreAdapter.collection
        set(value) {
            this.coreAdapter.collection = value
        }

    fun filter(predicate: (Any) -> Boolean) {
        this.coreAdapter.filter(predicate)
    }

    fun smoothScrollToLastPosition() {
        if (adapter == null)
            return

        if (adapter!!.itemCount != 0) {
            smoothScrollToPosition(adapter!!.itemCount - 1)
        }
    }

    fun resetFilter() {
        this.coreAdapter.resetFilter()
    }
}