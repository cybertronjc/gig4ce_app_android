package com.gigforce.common_ui.components.atoms

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class ChipGroupComponent : ChipGroup {

    init {
        isSelectionRequired = false
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

    fun addChips(list: List<ChipGroupModel>, isSingleSelection: Boolean, setFirstChecked: Boolean) {
        this.isSingleSelection = isSingleSelection
        val twelveDp: Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 24F,
            context.getResources().getDisplayMetrics()
        )
        list.forEach {
            var chipComponent = Chip(this.context)
            chipComponent.chipCornerRadius = twelveDp
            chipComponent.id = it.chipId
            chipComponent.text = it.text
            if (isSingleSelection){
                chipComponent.isClickable = true
                chipComponent.isCheckable = true
            }
            if (list.size == 1){
                setChipSelected(chipComponent.rootView, it)
            }
            chipComponent.setOnClickListener { it1 ->
                if (isSingleSelection){
                    this.clearCheck()
                    setAllChipsUnselected(list)
                    setChipSelected(it1, it)
                } else {
                    if (!it.isSelected) setChipSelected(it1, it) else setChipUnSelected(it1, it)
                }

            }
            this.addView(chipComponent)
        }
        if(setFirstChecked){
            setChipSelected((getChildAt(0) as Chip), list.get(0))
            //(getChildAt(0) as Chip).isChecked = true
        }


    }

    private fun setChipSelected(chip: View, chipGroupModel: ChipGroupModel) {
        Log.d("Here", "selected")
        checkedChangeListener?.onCheckedChangeListener(chipGroupModel)
        (chip as Chip).isChecked = true
        chipGroupModel.isSelected = true
    }
    private fun setChipUnSelected(chip: View, chipGroupModel: ChipGroupModel) {
        Log.d("Here", "UnSelected")
        checkedChangeListener?.onCheckedChangeListener(chipGroupModel)
        (chip as Chip).isChecked = false
        chipGroupModel.isSelected = false
    }

    private fun setAllChipsUnselected(list:List<ChipGroupModel> ) {
        for (i in 0 until this.childCount) {
            (getChildAt(i) as Chip).isChecked = false
            list.get(i).isSelected = false
            Log.d("Unselected chip", list.get(i).toString())
        }
        Log.d("Unselected list", list.toString())
    }


    interface OnCustomCheckedChangeListener {
        fun onCheckedChangeListener(model: ChipGroupModel)
    }
}