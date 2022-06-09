package com.gigforce.lead_management.ui.giger_info.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.gigforce.common_ui.ext.addMandatorySymbolToTextEnd
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.datamodels.client_activation.Dependency
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.LayoutApplicationChecklistItemBinding
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData
import com.gigforce.lead_management.ui.drop_selection.DropSelectionBottomSheetDialogFragment
import com.gigforce.lead_management.ui.giger_info.OnAddDocClickListener
import com.gigforce.lead_management.ui.giger_info.ShowCheckListDocsBottomSheet
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppCheckListRecyclerComponent(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
) {

    @Inject
    lateinit var buildConfig: IBuildConfig

    @Inject
    lateinit var navigation: INavigation

    private var viewBinding: LayoutApplicationChecklistItemBinding
    private lateinit var viewData: ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData
    var onAddDocClickListener: OnAddDocClickListener? = null

    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutApplicationChecklistItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    fun bind(data: ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData) {

        viewData = data
        if (viewData.isOptional) {
            viewBinding.checkListItemText.setText(viewData.checkName.capitalize())
        } else {
            viewBinding.checkListItemText.text = viewData.checkName.capitalize()
            viewBinding.checkListItemText.addMandatorySymbolToTextEnd()
        }
        //viewBinding.statusText.text = if (viewData.status == "Pending") context.getString(R.string.pending_lead) else ""
        //setStatusIcon(viewData.status)
        setStatusForDocs(viewData.docType, viewData.status, viewData.frontImage, viewData.backImage)
        //showFrontAndBackImage(viewData.status, viewData.frontImage, viewData.backImage)
        //showAddButtonForDocUpload(viewData.status)
    }

    private fun setStatusForDocs(type: String, status: String, frontImage: String?, backImage: String?) {
        if (status == "Pending" && isVerificationDoc(type)){
            viewBinding.statusDot.gone()
            viewBinding.statusText.gone()
            viewBinding.viewPhotoText.text = "ADD"
            viewBinding.viewPhotoText.visible()
        }else if (status == "Completed"){
            viewBinding.statusDot.gone()
            viewBinding.statusText.gone()
            viewBinding.statusIcon.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_pink_tick,null))
            if (frontImage?.isNullOrBlank() == false || backImage?.isNullOrBlank() == false){
                viewBinding.viewPhotoText.text = "VIEW PHOTO"
                viewBinding.viewPhotoText.visible()
            }else{
                viewBinding.viewPhotoText.gone()
            }
        }else{
            viewBinding.statusDot.visible()
            viewBinding.viewPhotoText.text = "Pending"
            viewBinding.viewPhotoText.gone()
            viewBinding.statusText.visible()
            viewBinding.statusText.setTextColor(ResourcesCompat.getColor(resources,R.color.pink_text,null))
            viewBinding.statusIcon.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_pending_icon,null))
        }
    }

    private fun showAddButtonForDocUpload(status: String) {
        if (status == "Pending"){
            viewBinding.viewPhotoText.text = "ADD"
            viewBinding.viewPhotoText.visible()
        }else{
            viewBinding.viewPhotoText.gone()
        }
    }

    private fun setStatusIcon(status: String) {
        if (status == "Pending") {
            viewBinding.statusDot.visible()
            viewBinding.statusText.setTextColor(ResourcesCompat.getColor(resources,R.color.pink_text,null))
            viewBinding.statusIcon.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_check_pending,null))
        } else {
            viewBinding.statusDot.gone()
            viewBinding.statusIcon.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_pink_tick,null))
        }
    }

    private fun showFrontAndBackImage(status: String, frontImage: String?, backImage: String?){
        //check if the status is completed
        if (status == "Completed"){
            if (frontImage?.isNullOrBlank() == false || backImage?.isNullOrBlank() == false){
                viewBinding.viewPhotoText.text = "VIEW PHOTO"
                viewBinding.viewPhotoText.visible()
            }else{
                viewBinding.viewPhotoText.gone()
            }

        }
    }

    fun getDBImageUrl(imagePath: String): String? {
        if (imagePath.isNotBlank()) {
            try {
                var modifiedString = imagePath
                if (!imagePath.startsWith("/"))
                    modifiedString = "/$imagePath"
                return buildConfig.getStorageBaseUrl() + modifiedString
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    fun openDialogToShowImage(path: StorageReference){
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
        val alertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.image_layout_dialog, null, false)
        materialAlertDialogBuilder.setView(alertDialogView)

        val docImage: ImageView = alertDialogView.findViewById(R.id.documentImage)
        Glide.with(context)
            .load(path)
            .fitCenter()
            .placeholder(getCircularProgressDrawable(context))
            .into(docImage)
        // Building the Alert dialog using materialAlertDialogBuilder instance
        materialAlertDialogBuilder
            .setCancelable(true)
            .show()
    }

    private fun getNavigationStr(data: String): String {
        when (data) {
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
                return "userinfo/addUserDrivingLicenseInfoFragment"
            }
            "learning" -> {
                return "learning/coursedetails"
            }
            "aadhar_card" -> {
                return "userinfo/addUserAadharCardInfoFragment"
            }
            "pan_card" -> {
                return "userinfo/addUserPanCardInfoFragment"
            }
            "bank_account" -> {
                return "userinfo/addUserBankDetailsInfoFragment"
            }
            "aadhar_card_questionnaire" -> {
                return "userinfo/addUserAadharCardInfoFragment"
            }
            "jp_hub_location" -> {
                return "client_activation/fragment_business_loc_hub"
            }
            "pf_esic" -> {
                return "client_activation/pfesicFragment"
            }
            else -> return ""
        }
    }

    private fun isVerificationDoc(type: String): Boolean {
        val veriArray = arrayListOf<String>("aadhar_card_questionnaire", "driving_licence", "pan_card", "bank_account", "profile_pic")
        return veriArray.contains(type)
    }


}