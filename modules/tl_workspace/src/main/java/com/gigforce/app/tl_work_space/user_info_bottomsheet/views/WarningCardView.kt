package com.gigforce.app.tl_work_space.user_info_bottomsheet.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.BottomsheetGigerInfoWarningCardviewBinding
import com.gigforce.app.tl_work_space.user_info_bottomsheet.GigerInformationDetailsBottomSheetFragmentViewEvents
import com.gigforce.app.tl_work_space.user_info_bottomsheet.models.UserInfoBottomSheetData
import com.google.android.material.card.MaterialCardView

class WarningCardView(
    context: Context,
    attrs: AttributeSet?
) : MaterialCardView(
    context,
    attrs
), View.OnClickListener {

    private lateinit var viewBinding: BottomsheetGigerInfoWarningCardviewBinding
    private var viewData: UserInfoBottomSheetData.RetentionComplianceWarningCardData? = null

    init {
        elevation = resources.getDimension(R.dimen.size_0dp)

        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        val px20 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            resources.displayMetrics
        ).toInt()

        val px10 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            10f,
            resources.displayMetrics
        ).toInt()
        params.setMargins(
            px20,
            px10,
            px20,
            px10
        )

        this.layoutParams = params

        radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            6f,
            resources.displayMetrics
        )
    }

    private fun inflate() {
        viewBinding = BottomsheetGigerInfoWarningCardviewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    private fun setListenersOnView() {
        viewBinding.reviewButton.setOnClickListener(this)
    }

    fun bind(data: UserInfoBottomSheetData.RetentionComplianceWarningCardData) {
        viewData = data
        setCardBackgroundColor(
            Color.parseColor(data.backgroundColorCode)
        )

        viewBinding.imageView.loadImage(
            data.icon
        )
        viewBinding.titleTextView.text = data.warningText

        if (data.actionButton != null) {
            viewBinding.reviewButton.isVisible = true
            viewBinding.reviewButton.text = data.actionButton.text
        } else {
            viewBinding.reviewButton.isVisible = false
        }
    }

    override fun onClick(v: View?) {
        val data = viewData ?: return
        val actionButtonData = viewData?.actionButton ?: return

        data.viewModel.setEvent(
            GigerInformationDetailsBottomSheetFragmentViewEvents.ActionButtonClicked(
                actionButtonData
            )
        )
    }

}