package com.gigforce.app.tl_work_space.user_info_bottomsheet.views

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.BottomsheetGigerInfoBusinessUserDetailsBinding
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData
import com.gigforce.common_ui.TextDrawable
import com.google.android.material.card.MaterialCardView

class UserDetailBusinessAndUserDetailsView(
    context: Context,
    attrs: AttributeSet?
) : MaterialCardView(
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

        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            resources.displayMetrics
        ).toInt()
        params.setMargins(
            px,
            px,
            px,
            px
        )

        this.layoutParams = params

        elevation = resources.getDimension(R.dimen.size_0dp)
        radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            resources.displayMetrics
        )
        setCardBackgroundColor(
            Color.parseColor("#E9F0FE")
        )
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
        val businessNameInitial = if(data.business.isNotBlank()) data.business[0].uppercase() else "C"

        if (!data.businessIcon.isNullOrBlank()) {
            this.businessImageIv.loadImageIfUrlElseTryFirebaseStorage(
                data.businessIcon
            )
        } else {
            val drawable = TextDrawable.builder().buildRound(
                businessNameInitial,
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
            this.gigerInfoBusinessItemContainer.addView(view)
            view.bind(it)
        }
    }

}