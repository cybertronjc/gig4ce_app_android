package com.gigforce.app.tl_work_space.user_info_bottomsheet.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.gigforce.app.tl_work_space.databinding.BottomsheetGigerInfoBusinessUserDetailsInfoItemBinding
import com.gigforce.app.tl_work_space.user_info_bottomsheet.GigerInformationDetailsBottomSheetFragmentViewEvents
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData
import com.google.android.material.card.MaterialCardView

class UserDetailBusinessAndUserDetailsItemView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
), View.OnClickListener {

    private lateinit var viewBinding: BottomsheetGigerInfoBusinessUserDetailsInfoItemBinding
    private var data: UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem? =
        null

    init {
        setDefault()
        inflate()
        setOnClickListeners()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = BottomsheetGigerInfoBusinessUserDetailsInfoItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    private fun setOnClickListeners() {
        viewBinding.actionIcon.setOnClickListener(this)
    }

    fun bind(
        data: UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData.UserDetailsBusinessAndUserDetailsDataItem
    ) = viewBinding.apply {
        this@UserDetailBusinessAndUserDetailsItemView.data = data

        this.imageView.loadImage(
            data.icon
        )
        this.titleTextView.text = data.key
        this.valueTextView.text = ": ${data.value}"


        if (data.actionIcon != null) {
            this.actionIcon.isVisible = true
            this.actionIcon.loadImage(data.actionIcon.icon)
        } else {
            this.actionIcon.isVisible = false
        }
    }

    override fun onClick(v: View?) {
        data?.viewModel?.setEvent(
            GigerInformationDetailsBottomSheetFragmentViewEvents.ActionButtonClicked(data!!.actionIcon!!)
        )
    }
}