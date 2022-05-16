package com.gigforce.app.tl_work_space.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.RecyclerViewSectionUpcomingGigersBinding
import com.gigforce.app.tl_work_space.home.TLWorkSpaceHomeViewContract
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.IViewHolder

class TLWorkspaceUpcomingGigersSectionView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: RecyclerViewSectionUpcomingGigersBinding
    private var viewData: TLWorkspaceRecyclerItemData.TLWorkspaceUpcomingGigersRecyclerItemData? = null

    init {
        elevation = resources.getDimension(R.dimen.card_elevation_mid)

        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerViewSectionUpcomingGigersBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    private fun setListenersOnView() {
        viewBinding.seeAllBtn.setOnClickListener(this)
    }

    override fun bind(data: Any?) {
        (data as TLWorkspaceRecyclerItemData.TLWorkspaceUpcomingGigersRecyclerItemData?)?.let {

            viewData = it
            inflateUpcomingGigers(
                it.upcomingGigers
            )
        }
    }

    private fun inflateUpcomingGigers(
        upcomingGigers: List<TLWorkspaceRecyclerItemData.UpcomingGigerItemData>
    ) = viewBinding.linearLayout.apply {
        removeAllViews()

        upcomingGigers.forEach {

           val view =  TLWorkspaceUpcomingGigersItemView(context,null)
           this.addView(view)
           view.bind(it)
        }
    }

    override fun onClick(v: View?) {
        viewData?.viewModel?.setEvent(
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.UpcomingGigersSectionEvent.SeeAllUpcomingGigersClicked
        )
    }
}