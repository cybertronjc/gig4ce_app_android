package com.gigforce.lead_management.ui.select_gig_application.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.lead_management.databinding.SelectGigApplicationSearchItemLayoutBinding
import com.gigforce.lead_management.databinding.SelectGigApplicationStatusItemLayoutBinding
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.models.JoiningListRecyclerItemData


class GigAppListSearchRecyclerItemView (
    context: Context,
    attrs: AttributeSet?
    ) : RelativeLayout(
    context,
    attrs
    ), IViewHolder, View.OnClickListener {

    private var viewBinding: SelectGigApplicationSearchItemLayoutBinding


    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = SelectGigApplicationSearchItemLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    }

    override fun bind(data: Any?) {

        data?.let {
            val searchText = it as GigAppListRecyclerItemData.GigAppListSearchRecyclerItemData
            viewBinding.searchBar.setText(searchText.search)
        }
    }

    override fun onClick(p0: View?) {

    }
}