package com.gigforce.app.tl_work_space.user_info_bottomsheet.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.BottomsheetGigerInfoBusinessUserDetailsBinding
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData
import com.gigforce.common_ui.TextDrawable
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.capitalizeWords
import com.google.android.material.card.MaterialCardView

class UserDetailBusinessAndUserDetailsView(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(
    context,
    attrs
) {

    private lateinit var viewBinding: BottomsheetGigerInfoBusinessUserDetailsBinding

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = BottomsheetGigerInfoBusinessUserDetailsBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    fun bind(
        data: UserInfoBottomSheetData.UserDetailsBusinessAndUserDetailsData
    ) = viewBinding.apply {

        this.businessNameTextview.text = data.business

        if (!data.businessIcon.isNullOrBlank()) {
            this.businessImageIv.loadImageIfUrlElseTryFirebaseStorage(
                data.businessIcon
            )
        } else {
            val drawable = TextDrawable.builder().buildRound(
                data.business.capitalizeFirstLetter()[0].toString(),
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.lipstick,
                    null
                )
            )
            this.businessImageIv.setImageDrawable(drawable)
        }

        this.gigerInfoBusinessItemContainer.removeAllViews()
        data.dataItems.forEach {

            val view = UserDetailBusinessAndUserDetailsItemView(context, null)
            addView(view)
            view.bind(it)
        }
    }

}