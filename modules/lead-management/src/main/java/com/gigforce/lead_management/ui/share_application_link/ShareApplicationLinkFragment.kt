package com.gigforce.lead_management.ui.share_application_link

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.common_ui.views.GigforceToolbar
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentLeadManagementReferralBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ShareApplicationLinkFragment : BaseFragment2<FragmentLeadManagementReferralBinding>(
    fragmentName = "ShareApplicationLinkFragment",
    layoutId = R.layout.fragment_lead_management_referral,
    statusBarColor = R.color.lipstick_2
) {

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: ShareApplicationLinkViewModel by viewModels()

    //Data
    private var shareType: String = ShareReferralType.SHARE_SIGNUP_LINK
    private lateinit var jobProfileId: String
    private lateinit var jobProfileName: String
    private var userUid: String? = null
    private var name: String? = null
    private var mobile: String? = null
    private var tradeName: String? = null

    override fun viewCreated(
        viewBinding: FragmentLeadManagementReferralBinding,
        savedInstanceState: Bundle?
    ) {

        getDataFrom(
            arguments,
            savedInstanceState
        )
        initToolbar(viewBinding.toolbar)
        initListeners(viewBinding)
        initViewModel()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {

            jobProfileId =
                it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ID) ?: return@let
            jobProfileName =
                it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_NAME) ?: return@let
            shareType = it.getString(LeadManagementConstants.INTENT_EXTRA_SHARE_TYPE) ?: return@let
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID)
            name = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_NAME)
            mobile = it.getString(LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER)
            tradeName = it.getString(LeadManagementConstants.INTENT_EXTRA_TRADE_NAME)
        }

        savedInstanceState?.let {

            jobProfileId =
                it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ID) ?: return@let
            jobProfileName =
                it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_NAME) ?: return@let
            shareType = it.getString(LeadManagementConstants.INTENT_EXTRA_SHARE_TYPE) ?: return@let
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID)
            name = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_NAME)
            mobile = it.getString(LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER)
            tradeName = it.getString(LeadManagementConstants.INTENT_EXTRA_TRADE_NAME)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ID, jobProfileId)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_NAME, jobProfileName)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_SHARE_TYPE, shareType)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_ID, userUid)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_NAME, name)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER, mobile)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_TRADE_NAME, tradeName)
    }

    private fun initToolbar(toolbar: GigforceToolbar) = toolbar.apply{
        val shareTitle = if (shareType == ShareReferralType.SHARE_JOB_PROFILE_LINK) {
            "Share Job Profile"
        } else {
            "Share Application Link"
        }
        showTitle(shareTitle)
        hideActionMenu()
        setBackButtonListener{
            activity?.onBackPressed()
        }
    }

    private fun initListeners(
        viewBinding: FragmentLeadManagementReferralBinding
    ) = viewBinding.apply {

        PushDownAnim
            .setPushDownAnimTo(sendViaWhatsappLayout)
            .setOnClickListener {

                if (shareType == ShareReferralType.SHARE_JOB_PROFILE_LINK) {

                    viewModel.sendJobProfileReferralLink(
                        userUid = userUid!!,
                        jobProfileId = jobProfileId,
                        jobProfileName = jobProfileName,
                        tradeName = tradeName ?: ""
                    )
                } else {
                    viewModel.sendAppReferralLink(
                        name = name ?: "",
                        mobileNumber = mobile!!,
                        jobProfileId = jobProfileId,
                        jobProfileName = jobProfileName,
                        tradeName = tradeName ?: ""
                    )
                }
            }

        PushDownAnim
            .setPushDownAnimTo(sendViaOtherApps)
            .setOnClickListener {

                if (shareType == ShareReferralType.SHARE_JOB_PROFILE_LINK) {

                    viewModel.sendJobProfileReferralLinkViaOtherApps(
                        userUid = userUid!!,
                        jobProfileId = jobProfileId,
                        jobProfileName = jobProfileName,
                        tradeName = tradeName ?: ""
                    )
                } else {
//                    viewModel.cre(
//                        name = name ?: "",
//                        mobileNumber = mobile!!,
//                        jobProfileId = jobProfileId,
//                        jobProfileName = jobProfileName
//                    )
                }

            }
    }

    private fun initViewModel() {
        viewModel.referralViewState
            .observe(viewLifecycleOwner, {
                val referralState = it ?: return@observe

                when (referralState) {
                    ShareReferralViewState.DocumentUpdatedAndReferralShared -> {
                        viewBinding.pbReferralsFrag.gone()

                        Toast.makeText(requireContext(),
                            "Link Shared",
                            Toast.LENGTH_SHORT
                        ).show()

                        navigation.popBackStack(
                            LeadManagementNavDestinations.FRAGMENT_JOINING,
                            false
                        )
                    }
                    is ShareReferralViewState.ErrorInCreatingOrUpdatingDocument -> {
                        viewBinding.pbReferralsFrag.gone()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Unable to share")
                            .setMessage(referralState.error)
                            .setPositiveButton("Okay") { _, _ -> }
                            .show()
                    }
                    ShareReferralViewState.SharingAndUpdatingJoiningDocument -> {
                        viewBinding.pbReferralsFrag.visible()
                    }
                    is ShareReferralViewState.OpenWhatsAppToShareDocumentSharingDocument -> {
                        viewBinding.pbReferralsFrag.gone()
                        shareViaWhatsApp(referralState.shareLink)
                    }
                    is ShareReferralViewState.OpenOtherAppsToShareDocumentSharingDocument -> {
                        viewBinding.pbReferralsFrag.gone()
                        shareToAnyApp(referralState.shareLink)
                    }
                    is ShareReferralViewState.UnableToCreateShareLink -> {
                        viewBinding.pbReferralsFrag.gone()
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Unable to share")
                            .setMessage(referralState.error)
                            .setPositiveButton("Okay") { _, _ -> }
                            .show()
                    }
                }
            })
    }

    private fun shareViaWhatsApp(url: String) {

        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "image/png"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, url)
        val bitmap =
            BitmapFactory.decodeResource(requireContext().resources, R.drawable.bg_gig_type)

        //save bitmap to app cache folder

        //save bitmap to app cache folder
        val outputFile = File(requireContext().cacheDir, "share" + ".png")
        val outPutStream = FileOutputStream(outputFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)
        outPutStream.flush()
        outPutStream.close()
        outputFile.setReadable(true, false)
        whatsappIntent.putExtra(
            Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                outputFile
            )
        )

        try {
            requireActivity().startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=com.whatsapp")
                )
            )
        }
    }

    private fun shareToAnyApp(url: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/png"
            shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.app_name)
            )
            val shareMessage = getString(R.string.looking_for_dynamic_working_hours) + " " + url
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            val bitmap =
                BitmapFactory.decodeResource(requireContext().resources, R.drawable.bg_gig_type)

            //save bitmap to app cache folder

            //save bitmap to app cache folder
            val outputFile = File(requireContext().cacheDir, "share" + ".png")
            val outPutStream = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)
            outPutStream.flush()
            outPutStream.close()
            outputFile.setReadable(true, false)
            shareIntent.putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    outputFile
                )
            )
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }
}