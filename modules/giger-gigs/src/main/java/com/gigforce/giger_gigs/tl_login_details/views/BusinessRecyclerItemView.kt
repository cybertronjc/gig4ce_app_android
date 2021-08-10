package com.gigforce.giger_gigs.tl_login_details.views

import android.content.Context

import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
    ), View.OnClickListener {

    private lateinit var viewBinding: LayoutBusinessNameRecyclerItemBinding
    lateinit var  businessDataFinal : BusinessListRecyclerItemData.BusinessRecyclerItemData

    var quantityTextChangeListener: QuantityTextChangeListener? = null
    var quantityTag  = ""

    fun setOnQuantityTextChangeListener(listener: QuantityTextChangeListener, tag: String) {
        this.quantityTextChangeListener = listener
        this.quantityTag = tag
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

    fun showData(businessData: BusinessListRecyclerItemData.BusinessRecyclerItemData){
        businessDataFinal = businessData
        var count = 0
        viewBinding.businessName.text = businessData.businessName ?: "Business N/A"
        viewBinding.jobProfileName.text = businessData.jobProfileName ?: "Job Profile N/A"
        if (viewBinding.jobProfileName.text.equals("null")){
            viewBinding.jobProfileName.visibility = View.INVISIBLE
        } else {
            viewBinding.jobProfileName.visibility = View.VISIBLE
        }

        if (businessData.itemView == 1){
            viewBinding.loginCount.isEnabled = false
            viewBinding.plusIcon.isEnabled = false
            viewBinding.minusIcon.isEnabled = false
            viewBinding.plusIcon.visibility = View.INVISIBLE
            viewBinding.minusIcon.visibility = View.INVISIBLE
            viewBinding.enterQuantity.visibility = View.VISIBLE
            viewBinding.loginCount.visibility = View.VISIBLE

            if (businessData.loginCount == null || businessData.loginCount == -1){
                count = -1
                viewBinding.enterQuantity.visibility = View.VISIBLE
                viewBinding.plusMinusLayout.visibility = View.INVISIBLE
            } else {
                count = businessData.loginCount!!
                viewBinding.enterQuantity.visibility = View.INVISIBLE
                viewBinding.plusMinusLayout.visibility = View.VISIBLE
                viewBinding.loginCount.setText(businessData.loginCount.toString())
            }
        } else {
            viewBinding.loginCount.isEnabled = true
            viewBinding.plusIcon.isEnabled = true
            viewBinding.minusIcon.isEnabled = true
            if (businessData.loginCount == null || businessData.loginCount == -1){
                count = -1
                viewBinding.enterQuantity.visibility = View.VISIBLE
                viewBinding.plusMinusLayout.visibility = View.INVISIBLE
            } else {
                count = businessData.loginCount!!
                viewBinding.enterQuantity.visibility = View.INVISIBLE
                viewBinding.plusMinusLayout.visibility = View.VISIBLE
                viewBinding.loginCount.setText(businessData.loginCount.toString())
            }
        }


        viewBinding.minusIcon.setOnClickListener {
            if (count != 0  && viewBinding.loginCount.text.toString().isNotEmpty()){
//                    businessData.loginCount = viewBinding.loginCount.text.toString().toInt()
                count--
                Log.d("clicked", "minus $count")
                viewBinding.loginCount.setText(count.toString())
            }
        }

        viewBinding.plusIcon.setOnClickListener {
            if (businessData.loginCount != null && viewBinding.loginCount.text.toString().isNotEmpty()){
//                    businessData.loginCount = viewBinding.loginCount.text.toString().toInt()
                count++
                Log.d("clicked", "plus $count")
                viewBinding.loginCount.setText(count.toString())
            } else  {
                count++
                Log.d("clicked", "plus $count")
                viewBinding.loginCount.setText(count.toString())
            }
        }

        viewBinding.enterQuantity.setOnClickListener {
            viewBinding.loginCount.isEnabled = true
            viewBinding.plusIcon.isEnabled = true
            viewBinding.minusIcon.isEnabled = true
            viewBinding.plusMinusLayout.visibility = View.VISIBLE
            viewBinding.enterQuantity.visibility = View.INVISIBLE
            count = 0
            viewBinding.loginCount.setText(count.toString())
        }

            viewBinding.loginCount.onTextChanged {
                //update data in viewmodel
                    //businessData.addNewLoginSummaryViewModel.updateList(businessData.businessId, it)
                if (it.isEmpty()){
                    count = 0
                }else {
                    it.toIntOrNull()?.let {
                        count = it
                    }

                }
                quantityTextChangeListener?.onQuantityTextChanged(it, quantityTag)
            }
    }

    //fun setQuantityTextChangeListener(onTextChangeListener: EditText.)

    fun getTLLoginSummary(): BusinessListRecyclerItemData.BusinessRecyclerItemData{
        val gigerCount = if (viewBinding.loginCount.text.toString().isEmpty()) null else
            viewBinding.loginCount.text.toString().toIntOrNull() ?: 0
        return BusinessListRecyclerItemData.BusinessRecyclerItemData(
            businessDataFinal.businessId,
            businessName = businessDataFinal.businessName,
            legalName = businessDataFinal.legalName,
            jobProfileId = businessDataFinal.jobProfileId,
            jobProfileName = businessDataFinal.jobProfileName,
            loginCount = gigerCount,
            updatedBy = businessDataFinal.updatedBy,
            businessDataFinal.addNewLoginSummaryViewModel,
            businessDataFinal.itemView
        )

    }

    override fun onClick(p0: View?) {

    }

    interface QuantityTextChangeListener {

        fun onQuantityTextChanged(text: String, tag: String)
    }
}