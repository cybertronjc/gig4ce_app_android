package com.gigforce.lead_management.ui.input_salary_components.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.common_ui.viewdatamodels.leadManagement.InputSalaryDataItem
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.lead_management.databinding.LayoutInputSalaryComponentViewBinding
import com.gigforce.lead_management.ui.select_gig_application.views.GigAppListSearchRecyclerItemView


class InputSalaryComponentView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
) {
    private var viewBinding: LayoutInputSalaryComponentViewBinding
    private lateinit var viewData: InputSalaryDataItem

//    var amountTextChangeListener: AmountTextChangeListener? = null
//
//    fun setOnAmountTextChangeListener(listener: AmountTextChangeListener) {
//        this.amountTextChangeListener = listener
//    }

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

//        viewBinding.editText.onTextChanged {
//            if (it.isNotEmpty()){
//                viewData.amount = it.toInt()
//            } else {
//                viewData.amount = 0
//            }
//            //amountTextChangeListener?.onAmountTextChanged(it)
//        }
    }

    fun showData(salaryComponentData: InputSalaryDataItem) = viewBinding.apply {
        viewData = salaryComponentData

        if (viewData.name?.isNotEmpty() == true){
            titleTextview.text = viewData.business?.name
            viewBinding.titleTextview.addMandatorySymbolToTextEnd()
        }

        if (viewData.amount != -1){
            editText.setText(viewData.amount.toString())
        }

    }
//
//    fun setCopyClickListener(
//        listener: OnClickListener
//    ) {
//        this.viewBinding.editText.onTextChanged { this }
//    }
//
//    interface AmountTextChangeListener {
//
//        fun onAmountTextChanged(text: String)
//    }
}