package com.gigforce.app.tl_work_space.user_info_bottomsheet.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.BottomsheetGigerInfoActionButtonBinding
import com.gigforce.app.tl_work_space.user_info_bottomsheet.GigerInformationDetailsBottomSheetFragmentViewEvents
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData

class UserDetailsActionButtonView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), View.OnClickListener {

    private lateinit var viewBinding: BottomsheetGigerInfoActionButtonBinding
    private var viewData: UserInfoBottomSheetData.UserInfoActionButtonData? = null

    init {
        elevation = resources.getDimension(R.dimen.card_elevation_mid)

        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
        viewBinding.floatingActionButton.setOnClickListener(this)
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = BottomsheetGigerInfoActionButtonBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    fun bind(
        data: UserInfoBottomSheetData.UserInfoActionButtonData
    ) {
        viewData = data

        viewBinding.floatingActionButton.setImageResource(
            data.icon
        )
        viewBinding.textView.text = data.text
    }

    override fun onClick(v: View?) {
        val data = viewData ?: return
        data.viewModel.setEvent(
            GigerInformationDetailsBottomSheetFragmentViewEvents.ActionButtonClicked(data)
        )
    }
}