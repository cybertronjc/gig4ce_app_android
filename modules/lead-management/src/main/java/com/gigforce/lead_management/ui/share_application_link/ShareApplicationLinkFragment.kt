package com.gigforce.lead_management.ui.share_application_link

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.views.GigforceToolbar
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentLeadManagementReferralBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import com.gigforce.lead_management.ui.LeadManagementSharedViewModelState
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

    companion object{
        const val REQUEST_CODE_SHARE_VIA_WHATSAPP = 101
        const val REQUEST_CODE_SHARE_VIA_OTHER_APPS = 102
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: ShareApplicationLinkViewModel by viewModels()
    private val sharedViewModel : LeadManagementSharedViewModel by activityViewModels()

    //Data
    private var shareType: String = ShareReferralType.SHARE_SIGNUP_LINK
    private lateinit var jobProfileId: String
    private lateinit var jobProfileName: String
    private var jobProfileIcon: String? = null
    private var userUid: String? = null
    private var name: String? = null
    private var mobile: String? = null
    private var tradeName: String? = null
    private var joiningId: String? = null

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

            jobProfileIcon = it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ICON)
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID)
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

            jobProfileIcon = it.getString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ICON)
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID)
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
        outState.putString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID, joiningId)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ICON, jobProfileIcon)
    }

    private fun initToolbar(toolbar: GigforceToolbar) = toolbar.apply{
        val shareTitle = if (shareType == ShareReferralType.SHARE_JOB_PROFILE_LINK) {
            context.getString(R.string.share_job_lead)
        } else {
            context.getString(R.string.share_app_link_lead)
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
                openWhatsAppForSharingLink()
            }

        PushDownAnim
            .setPushDownAnimTo(sendViaOtherApps)
            .setOnClickListener {

                if (shareType == ShareReferralType.SHARE_JOB_PROFILE_LINK) {

                    viewModel.sendJobProfileReferralLinkViaOtherApps(
                        userUid = userUid!!,
                        jobProfileId = jobProfileId,
                        jobProfileName = jobProfileName,
                        tradeName = tradeName ?: "",
                        joiningId = joiningId,
                        jobProfileIcon = jobProfileIcon ?: ""
                    )
                } else {
                    viewModel.sendAppReferralLinkViaOtherApps(
                        name = name ?: "",
                        mobileNumber = mobile!!,
                        jobProfileId = jobProfileId,
                        jobProfileName = jobProfileName,
                        tradeName = tradeName ?: "",
                        jobProfileIcon = jobProfileIcon ?: ""
                    )
                }
            }
    }

    private fun openWhatsAppForSharingLink() {
        if (shareType == ShareReferralType.SHARE_JOB_PROFILE_LINK) {

            viewModel.sendJobProfileReferralLink(
                userUid = userUid!!,
                jobProfileId = jobProfileId,
                jobProfileName = jobProfileName,
                tradeName = tradeName ?: "",
                joiningId = joiningId,
                jobProfileIcon = jobProfileIcon ?: ""
            )
        } else {
            viewModel.sendAppReferralLink(
                name = name ?: "",
                mobileNumber = mobile!!,
                jobProfileId = jobProfileId,
                jobProfileName = jobProfileName,
                tradeName = tradeName ?: "",
                jobProfileIcon = jobProfileIcon ?: ""
            )
        }
    }

    private fun initViewModel() {
        viewModel.referralViewState
            .observe(viewLifecycleOwner, {
                val referralState = it ?: return@observe

                when (referralState) {
                    is ShareReferralViewState.DocumentUpdatedAndReferralShared -> {
                        viewBinding.pbReferralsFrag.gone()

                        Toast.makeText(requireContext(),
                            getString(R.string.link_shared_lead),
                            Toast.LENGTH_SHORT
                        ).show()
                        ReferralLinkSharedResultDialogFragment.launchSuccess(childFragmentManager,referralState.shareLink)
                    }
                    is ShareReferralViewState.ErrorInCreatingOrUpdatingDocument -> {
                        viewBinding.pbReferralsFrag.gone()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.unable_to_share_lead))
                            .setMessage(referralState.error)
                            .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
                            .show()
                    }
                    ShareReferralViewState.SharingAndUpdatingJoiningDocument -> {
                        viewBinding.pbReferralsFrag.visible()
                    }
                    is ShareReferralViewState.OpenWhatsAppToShareDocumentSharingDocument -> {
                        viewBinding.pbReferralsFrag.gone()
                        ReferralLinkSharedResultDialogFragment.launchError(childFragmentManager,referralState.shareLink)
                    }
                    is ShareReferralViewState.OpenOtherAppsToShareDocumentSharingDocument -> {
                        viewBinding.pbReferralsFrag.gone()
                        shareToAnyApp(referralState.shareLink)
                    }
                    is ShareReferralViewState.UnableToCreateShareLink -> {
                        viewBinding.pbReferralsFrag.gone()
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.unable_to_share_lead))
                            .setMessage(referralState.error)
                            .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
                            .show()
                    }
                }
            })

        sharedViewModel
            .viewState
            .observe(viewLifecycleOwner, Observer {
                it ?: return@Observer

                when (it) {
                    LeadManagementSharedViewModelState.OnReferralDialogOkayClicked -> {
                        if(!isAdded) return@Observer
                        navigateBackToJoiningListScreen()
                    }
                    is LeadManagementSharedViewModelState.OnReferralDialogSendLinkViaLocalWhatsappClicked -> {
                        if(!isAdded) return@Observer
                        shareViaWhatsApp(it.link)
                    }
                }
            })
    }

    private fun navigateBackToJoiningListScreen() {
        navigation.popBackStack(
            LeadManagementNavDestinations.FRAGMENT_JOINING,
            false
        )
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
            requireActivity().startActivityForResult(
                whatsappIntent,
                REQUEST_CODE_SHARE_VIA_WHATSAPP
                )
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
            val shareMessage = getString(R.string.looking_for_dynamic_working_hours_lead) + " " + url
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
            startActivityForResult(
                Intent.createChooser(shareIntent, "choose one"),
                REQUEST_CODE_SHARE_VIA_OTHER_APPS
                )
        } catch (e: Exception) {
            //e.toString();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        navigateBackToJoiningListScreen()
    }
}