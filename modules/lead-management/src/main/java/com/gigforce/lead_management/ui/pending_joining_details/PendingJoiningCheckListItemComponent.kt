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
import com.gigforce.core.datamodels.client_activation.Dependency
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

    private fun getNavigationStr(data: Dependency): String {
        when (data.type) {
            "profile_pic" -> {
                return "profile"
            }
            "about_me" -> {
                return "profile/addBio"
            }
            "questionnaire" -> {
                return "learning/questionnair"
            }
            "driving_licence" -> {
                return "verification/drivinglicenseimageupload"
            }
            "learning" -> {
                return "learning/coursedetails"
            }
            "aadhar_card" -> {
                return "verification/aadhaarcardimageupload"
            }
            "pan_card" -> {
                return "verification/pancardimageupload"
            }
            "bank_account" -> {
                return "verification/bank_account_fragment"
            }
            "aadhar_card_questionnaire" -> {
                return "verification/AadharDetailInfoFragment"
            }
            "jp_hub_location" -> {
                return "client_activation/fragment_business_loc_hub"
            }
            "aadhar_hub_questionnaire" -> {
                return "client_activation/joining_form"
            }
            "pf_esic" -> {
                return "client_activation/pfesicFragment"
            }
            else -> return ""
        }
    }


    override fun onClick(v: View?) {

        var navigationsForBundle = ArrayList<String>()
        var startCreateNavBundle = false
        var title = viewData.title ?: ""
        var typeForQuestionair = viewData.checkListItemType
        var courseId = viewData.courseId ?: ""

        if(viewData.options != null) {

            if (startCreateNavBundle && !viewData.options!!.isDone) {
                navigationsForBundle.add(getNavigationStr(viewData.options!!))
            }
            if (viewData.options!!.type == viewData.options!!.type) {
                startCreateNavBundle = true
            }
            if (viewData.options!!.type == "questionnaire") {
                title = viewData.options!!.title ?: ""
                typeForQuestionair = viewData.options!!.type ?: ""
            }
            //code to get data for title type
        }

        var bundleForFragment = bundleOf(
            StringConstants.NAVIGATION_STRING_ARRAY.value to navigationsForBundle,
            StringConstants.FROM_CLIENT_ACTIVATON.value to true,
            StringConstants.ACTION.value to 1,
            StringConstants.JOB_PROFILE_ID.value to jobProfileId,
            StringConstants.TITLE.value to title,
            StringConstants.TYPE.value to typeForQuestionair,
            INTENT_EXTRA_COURSE_ID to courseId
        )
        when (viewData.checkListItemType) {
            "profile_pic" -> {
                navigation?.navigateTo(
                    "profile", bundleForFragment
                )

            }
            "about_me" -> {
                navigation?.navigateTo(
                    "profile/addBio", bundleForFragment
                )
            }
            "questionnaire" -> navigation?.navigateTo(
                "learning/questionnair", bundleForFragment
            )
            "driving_licence" -> navigation?.navigateTo(
                "verification/drivinglicenseimageupload",
                bundleForFragment
            )
            "learning" ->

                navigation?.navigateTo(
                    "learning/coursedetails",
                    bundleForFragment
                )

            "aadhar_card" -> navigation?.navigateTo(
                "verification/aadhaarcardimageupload",

                bundleForFragment
            )

            "pan_card" -> navigation?.navigateTo(
                "verification/pancardimageupload",
                bundleForFragment
            )

            "bank_account" -> navigation?.navigateTo(
                "verification/bank_account_fragment",
                bundleForFragment
            )

            "aadhar_card_questionnaire" -> navigation?.navigateTo(
                "verification/AadharDetailInfoFragment",
                bundleForFragment
            )

            "jp_hub_location" -> navigation?.navigateTo(
                "client_activation/fragment_business_loc_hub",
                bundleForFragment
            )

            "aadhar_hub_questionnaire" -> navigation?.navigateTo(
                "client_activation/joining_form",
                bundleForFragment
            )

            "pf_esic" -> navigation?.navigateTo(
                "client_activation/pfesicFragment",
                bundleForFragment
            )
        }


    }
}