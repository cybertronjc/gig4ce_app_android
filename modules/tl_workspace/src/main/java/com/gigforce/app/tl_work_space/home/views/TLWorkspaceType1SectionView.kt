package com.gigforce.app.tl_work_space.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.TLWorkspaceHomeSection
import com.gigforce.app.tl_work_space.databinding.RecyclerViewSectionType1Binding
import com.gigforce.app.tl_work_space.home.TLWorkSpaceHomeUiEvents
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.IViewHolder

class TLWorkspaceType1SectionView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {
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

        setListeners()
    }

    private fun setListeners() {
        viewBinding.filterTextview.setOnClickListener(this)
    }

    override fun bind(data: Any?) {
        (data as TLWorkspaceRecyclerItemData.TLWorkspaceType1RecyclerItemData?)?.let {
            viewData = it

            viewBinding.titleTextview.text = it.sectionTitle.capitalizeFirstLetter()
            viewBinding.filterTextview.isVisible = it.currentFilter != null
            viewBinding.filterTextview.text = it.currentFilter?.getFilterString()

            viewBinding.recyclerView.layoutManager = GridLayoutManager(
                context,
                it.itemData.size
            )
            viewBinding.recyclerView.collection = it.itemData
        }
    }

    override fun onClick(v: View?) {
        val anchorView = v ?: return

        viewData?.viewModel?.setEvent(
            TLWorkSpaceHomeUiEvents.OpenFilter(
                sectionOpenFilterClickedFrom = TLWorkspaceHomeSection.fromId(viewData!!.sectionId),
                anchorView = anchorView
            )
        )
    }

}