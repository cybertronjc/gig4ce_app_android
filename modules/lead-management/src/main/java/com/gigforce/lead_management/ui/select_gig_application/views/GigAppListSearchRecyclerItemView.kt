package com.gigforce.lead_management.ui.select_gig_application.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.lead_management.R
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
    var searchTextChangeListener: SearchTextChangeListener? = null

    fun setOnSearchTextChangeListener(listener: SearchTextChangeListener) {
        this.searchTextChangeListener = listener
    }

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = SelectGigApplicationSearchItemLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    }


    override fun bind(data: Any?) {

        data?.let { it ->
            val searchText = it as GigAppListRecyclerItemData.GigAppListSearchRecyclerItemData
            //viewBinding.searchBar.setText(searchText.search)

            //viewBinding.searchBar.requestFocus()
            viewBinding.searchBar.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    searchText.selectGigAppViewModel.searchOtherApplications(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })
        }
    }

    override fun onClick(p0: View?) {

    }

    interface SearchTextChangeListener {

        fun onSearchTextChanged(text: String)
    }
}