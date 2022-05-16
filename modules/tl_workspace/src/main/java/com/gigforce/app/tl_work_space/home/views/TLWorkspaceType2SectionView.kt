package com.gigforce.app.tl_work_space.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.databinding.RecyclerViewSectionType2Binding
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.IViewHolder

class TLWorkspaceType2SectionView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), IViewHolder {

    private lateinit var viewBinding: RecyclerViewSectionType2Binding
    private var viewData: TLWorkspaceRecyclerItemData.TLWorkspaceType2RecyclerItemData? = null

    init {
        orientation = VERTICAL

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
        viewBinding = RecyclerViewSectionType2Binding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        (data as TLWorkspaceRecyclerItemData.TLWorkspaceType2RecyclerItemData?)?.let {
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