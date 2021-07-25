package com.gigforce.common_ui.components.atoms

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class ChipGroupComponent : ChipGroup {

    init {
        isSelectionRequired = true
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    var checkedChangeListener: OnCustomCheckedChangeListener? = null

    fun setOnCheckedChangeListener(listener: OnCustomCheckedChangeListener?) {
        this.checkedChangeListener = listener
    }

    fun addChips(list: List<ChipGroupModel>, isSingleSelection: Boolean) {
        this.isSingleSelection = isSingleSelection
        list.forEach {
            var chipComponent = Chip(this.context)
            chipComponent.id = it.chipId
            chipComponent.text = it.text
//            chipComponent.setOnClickListener { it1 ->
//                if(!it1.isSelected) setChipSelected(it1, it) else setChipUnSelected(it1, it)
//                checkedChangeListener?.onCheckedChangeListener(it)
//            }
            this.addView(chipComponent)
        }
        setChipSelected((getChildAt(0) as Chip), list.get(0))
        //(getChildAt(0) as Chip).isChecked = true

    }

    private fun setChipSelected(chip: View, chipGroupModel: ChipGroupModel) {
        (chip as Chip).isChecked = true
        chipGroupModel.isSelected = true
    }
    private fun setChipUnSelected(chip: View, chipGroupModel: ChipGroupModel) {
        (chip as Chip).isChecked = false
        chipGroupModel.isSelected = false
    }

    private fun setAllChipsUnselected() {
        for (i in 0 until this.childCount) {
            (getChildAt(i) as Chip).isChecked = false
        }
    }


    interface OnCustomCheckedChangeListener {
        fun onCheckedChangeListener(model: ChipGroupModel)
    }
}