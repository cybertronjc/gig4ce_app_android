package com.gigforce.app.tl_work_space.user_info_bottomsheet.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.BottomsheetGigerInfoUserDetailsActionButtonsViewBinding
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData

class UserDetailsAndActionButtonsView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
) {

    private lateinit var viewBinding: BottomsheetGigerInfoUserDetailsActionButtonsViewBinding
    private var viewData: UserInfoBottomSheetData.UserDetailsAndActionData? = null

    init {
        elevation = resources.getDimension(R.dimen.card_elevation_mid)

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
        viewBinding = BottomsheetGigerInfoUserDetailsActionButtonsViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }


    fun bind(
        data: UserInfoBottomSheetData.UserDetailsAndActionData
    ) {
        viewData = data

        viewBinding.userImageImageview.loadProfilePicture(
            data.profilePictureThumbnail,
            data.profilePicture
        )
        viewBinding.nameTextview.text = data.gigerName
        viewBinding.lastActiveTextview.text = data.lastActiveText

        viewBinding.actionButtonContainer.removeAllViews()
        data.actionButtons.forEach {

            val view = UserDetailsActionButtonView(context, null)
            viewBinding.actionButtonContainer.addView(view)
            view.bind(it)
        }
    }
}