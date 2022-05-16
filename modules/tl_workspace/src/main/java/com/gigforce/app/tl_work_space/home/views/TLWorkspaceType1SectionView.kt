package com.gigforce.app.tl_work_space.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.databinding.RecyclerViewSectionType1Binding
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.IViewHolder

class TLWorkspaceType1SectionView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), IViewHolder {
    private var viewData: TLWorkspaceRecyclerItemData.TLWorkspaceType1RecyclerItemData? = null
    private val viewBinding: RecyclerViewSectionType1Binding

    init {
        orientation = VERTICAL

        this.layoutParams =
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = RecyclerViewSectionType1Binding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        (data as TLWorkspaceRecyclerItemData.TLWorkspaceType1RecyclerItemData?)?.let {
            viewData = it

            viewBinding.titleTextview.text = it.sectionTitle.capitalizeFirstLetter()

            viewBinding.recyclerView.layoutManager = GridLayoutManager(
                context,
                it.noOfItemsToShowInGrid
            )
            viewBinding.recyclerView.collection = it.itemData
        }
    }

}