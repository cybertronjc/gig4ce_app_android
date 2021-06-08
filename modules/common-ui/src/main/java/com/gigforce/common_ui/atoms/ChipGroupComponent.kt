package com.gigforce.common_ui.atoms

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MarginLayoutParamsCompat
import com.gigforce.common_ui.R
import com.google.android.material.chip.ChipGroup
import java.util.*


class ChipGroupComponent : ChipGroup {
    var rows = 2
    var deque = LinkedList<Int>()

    constructor(context: Context?) : super(context)


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        extractAttributeValues(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :super(context, attrs, defStyleAttr){
        extractAttributeValues(context, attrs)
    }

    private fun extractAttributeValues(context: Context, attrs: AttributeSet) {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ChipGroupComponent, 0, 0)
//        rows = try {
//            ta.getInteger(R.styleable.CustomChipGroup_rows, 2)
//        } finally {
//            ta.recycle()
//        }
    }

    fun addChips(list: List<ChipComponent>){
        
    }
}