package com.gigforce.giger_gigs.tl_login_details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.core.IViewHolder
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

            viewBinding.businessName.text = businessData.businessName ?: "Business N/A"
            viewBinding.loginCount.setText(businessData.loginCount.toString() ?: "0")
            businessData.loginCount = viewBinding.loginCount.text.toString().toInt()

            viewBinding.minusIcon.setOnClickListener {
                if (businessData.loginCount != 0 && viewBinding.loginCount.text.toString().isNotEmpty()){
                    businessData.loginCount = viewBinding.loginCount.text.toString().toInt()
                    businessData.loginCount--
                    viewBinding.loginCount.setText(businessData.loginCount.toString() ?: "0")
                }
            }

            viewBinding.plusIcon.setOnClickListener {
                if ( viewBinding.loginCount.text.toString().isNotEmpty()){
                    businessData.loginCount = viewBinding.loginCount.text.toString().toInt()
                    businessData.loginCount++
                    viewBinding.loginCount.setText(businessData.loginCount.toString() ?: "0")
                }
            }
            businessData.loginCount = viewBinding.loginCount.text.toString().toInt()
        }

    }

    override fun onClick(p0: View?) {

    }
}