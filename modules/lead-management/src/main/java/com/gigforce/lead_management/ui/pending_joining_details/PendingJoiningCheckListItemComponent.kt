package com.gigforce.lead_management.ui.pending_joining_details

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import com.gigforce.common_ui.StringConstants
import com.gigforce.core.AppConstants.INTENT_EXTRA_COURSE_ID
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.LayoutPendingJoiningChecklistItemBinding
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData
import dagger.hilt.android.AndroidEntryPoint

class PendingJoiningCheckListItemComponent(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), View.OnClickListener {

    private var viewBinding: LayoutPendingJoiningChecklistItemBinding
    private lateinit var viewData: ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData

    private var navigation: INavigation? = null
    private var jobProfileId : String? = null

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutPendingJoiningChecklistItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        viewBinding.root.setOnClickListener(this)
    }

    fun bind(
        data: ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData,
        navigation: INavigation,
        jobProfileId: String
    ) {
        this.navigation = navigation
        this.jobProfileId = jobProfileId
        this.viewData = data

        if (viewData.isOptional) {
            viewBinding.checkListItemText.text = viewData.checkName
        } else {
            val txt = viewData.checkName + "<font color=\"red\"> *</font>"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                viewBinding.checkListItemText.setText(
                    Html.fromHtml(
                        txt,
                        Html.FROM_HTML_MODE_LEGACY
                    ), TextView.BufferType.SPANNABLE
                )
            } else {
                viewBinding.checkListItemText.setText(
                    Html.fromHtml(txt),
                    TextView.BufferType.SPANNABLE
                )
            }
        }
        setStatusIcon(viewData.status)
    }

    private fun setStatusIcon(status: String) {
        if (status == "Pending") {
            viewBinding.statusIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_check_pending,
                    null
                )
            )
        } else {
            viewBinding.statusIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_pink_tick,
                    null
                )
            )
        }
    }

    override fun onClick(v: View?) {
        when (viewData.checkListItemType) {
            "profile_pic" -> {
                navigation?.navigateTo(
                    "profile", bundleOf(
                        StringConstants.FROM_CLIENT_ACTIVATON.value to true,
                        StringConstants.ACTION.value to 1
                    )
                )
            }
            "about_me" -> {
                navigation?.navigateTo(
                    "profile/addBio", bundleOf(
                        StringConstants.FROM_CLIENT_ACTIVATON.value to true
                    )
                )
            }
            "questionnaire" -> navigation?.navigateTo(
                "learning/questionnair", bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to jobProfileId,
                    StringConstants.TITLE.value to viewData.options!!.title,
                    StringConstants.TYPE.value to viewData.options!!.type,
                    StringConstants.FROM_CLIENT_ACTIVATON.value to true
                )
            )
            "driving_licence" -> navigation?.navigateTo(
                "verification/drivinglicenseimageupload",
                bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
            )
            "learning" -> navigation?.navigateTo(
                "learning/coursedetails",
                bundleOf(
                    INTENT_EXTRA_COURSE_ID to viewData.options!!.courseId,
                    StringConstants.FROM_CLIENT_ACTIVATON.value to true
                )
            )

            "aadhar_card" -> navigation?.navigateTo(
                "verification/aadhaarcardimageupload",
                bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
            )

            "pan_card" -> navigation?.navigateTo(
                "verification/pancardimageupload",
                bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
            )

            "bank_account" -> navigation?.navigateTo(
                "verification/bank_account_fragment",
                bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
            )
        }
    }
}