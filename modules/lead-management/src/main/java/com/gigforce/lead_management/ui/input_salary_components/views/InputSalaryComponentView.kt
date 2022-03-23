package com.gigforce.lead_management.ui.input_salary_components.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.gigforce.common_ui.viewdatamodels.leadManagement.InputSalaryDataItem
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.lead_management.databinding.LayoutInputSalaryComponentViewBinding


class InputSalaryComponentView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
) {
    private var viewBinding: LayoutInputSalaryComponentViewBinding
    private lateinit var viewData: InputSalaryDataItem

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutInputSalaryComponentViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        viewBinding.editText.onTextChanged {
            if (it.isNotEmpty()){
                viewData.value = it.toInt()
            } else {
                viewData.value = 0
            }

        }
    }

    fun showData(salaryComponentData: InputSalaryDataItem) = viewBinding.apply {
        viewData = salaryComponentData

        if (viewData.name?.isNotEmpty() == true){
            titleTextview.text = viewData.name
        }

        if (viewData.value != 0){
            editText.setText(viewData.value.toString())
        }

    }
}