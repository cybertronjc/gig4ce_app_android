package com.gigforce.lead_management.gigeronboarding.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.SelectGigApplicationItemLayoutBinding
import com.gigforce.lead_management.databinding.SelectGigApplicationSearchItemLayoutBinding
import com.gigforce.lead_management.models.GigAppListRecyclerItemData


class GigAppListSearchRecyclerItemView (
    context: Context,
    attrs: AttributeSet?
    ) : RelativeLayout(
    context,
    attrs
    ), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: SelectGigApplicationSearchItemLayoutBinding


    init{
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.searchBar.setOnClickListener(this)
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = SelectGigApplicationSearchItemLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    }

    override fun bind(data: Any?) {

        data?.let {

        }
    }

    override fun onClick(p0: View?) {

    }
}