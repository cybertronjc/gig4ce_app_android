package com.gigforce.app.tl_work_space.payout.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.RecyclerViewGigerPayoutBusinessItemViewBinding
import com.gigforce.app.tl_work_space.payout.GigerPayoutFragmentViewEvents
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutScreenData
import com.gigforce.core.IViewHolder

class GigerPayoutBusinessItemView (
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: RecyclerViewGigerPayoutBusinessItemViewBinding
    private var viewData:GigerPayoutScreenData.BusinessItemData? = null

    init {

        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerViewGigerPayoutBusinessItemViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        viewBinding.root.setOnClickListener(this)
    }

    override fun bind(data: Any?) {
        (data as GigerPayoutScreenData.BusinessItemData?)?.let {
            viewData = it
            viewBinding.statusTv.text = "${it.businessName + " (" + it.count + ")"}"

            if (it.expanded) {

                viewBinding.statusTv.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.pink_text, null)
                )
                viewBinding.dropdownView.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_up, null)
                )
            } else {

                viewBinding.statusTv.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey, null)
                )
                viewBinding.dropdownView.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_drop, null)
                )
            }
        }
    }

    override fun onClick(
        p0: View?
    ) {
        viewData?.viewModel?.setEvent(
            GigerPayoutFragmentViewEvents.BusinessClicked(viewData!!.businessName)
        )
    }
}