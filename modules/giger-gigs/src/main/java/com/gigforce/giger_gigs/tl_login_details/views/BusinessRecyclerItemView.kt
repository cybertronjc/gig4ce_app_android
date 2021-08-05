package com.gigforce.giger_gigs.tl_login_details.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.LayoutBusinessNameRecyclerItemBinding
import com.gigforce.giger_gigs.models.BusinessListRecyclerItemData

class BusinessRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
    ) : FrameLayout(
    context,
    attrs
    ), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: LayoutBusinessNameRecyclerItemBinding

    var quantityTextChangeListener: QuantityTextChangeListener? = null

    fun setOnQuantityTextChangeListener(listener: QuantityTextChangeListener) {
        this.quantityTextChangeListener = listener
    }

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutBusinessNameRecyclerItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    }

    override fun bind(data: Any?) {
        data?.let {
            val businessData = it as BusinessListRecyclerItemData.BusinessRecyclerItemData
            var count = 0
            viewBinding.businessName.text = businessData.businessName ?: "Business N/A"
            if (businessData.loginCount == null || businessData.loginCount == -1){
                viewBinding.loginCount.setText("")
                count = -1
            } else {
                count = businessData.loginCount!!
                 viewBinding.loginCount.setText(businessData.loginCount.toString())

            }

            viewBinding.minusIcon.setOnClickListener {
                if (businessData.loginCount != null && businessData.loginCount != 0 && viewBinding.loginCount.text.toString().isNotEmpty()){
//                    businessData.loginCount = viewBinding.loginCount.text.toString().toInt()
                    count--
                    viewBinding.loginCount.setText(count.toString())
                }
            }

            viewBinding.plusIcon.setOnClickListener {
                if (businessData.loginCount != null && viewBinding.loginCount.text.toString().isNotEmpty()){
//                    businessData.loginCount = viewBinding.loginCount.text.toString().toInt()
                    count++
                    viewBinding.loginCount.setText(count.toString())
                } else if (count == -1){
                    count++
                    viewBinding.loginCount.setText(count.toString())
                }
            }
            //businessData.loginCount = viewBinding.loginCount.text.toString().toInt()
            if (businessData.itemView == 1){
                viewBinding.loginCount.isEnabled = false
                viewBinding.plusIcon.isEnabled = false
                viewBinding.minusIcon.isEnabled = false
            } else {
                viewBinding.loginCount.isEnabled = true
                viewBinding.plusIcon.isEnabled = true
                viewBinding.minusIcon.isEnabled = true
            }

//            Log.d("updated", "data : $businessData")

//            if (businessData.updatedBy == "null" || businessData.updatedBy === null){
//                viewBinding.root.background = resources.getDrawable(R.drawable.border_option_grey)
//            } else {
//                viewBinding.root.background = resources.getDrawable(R.drawable.business_item_background_pink)
//            }

            viewBinding.loginCount.onTextChanged {
                //update data in viewmodel
                    businessData.addNewLoginSummaryViewModel.updateList(businessData.businessId, it)
            }
        }

    }

    override fun onClick(p0: View?) {

    }

    interface QuantityTextChangeListener {

        fun onQuantityTextChanged(text: String)
    }
}