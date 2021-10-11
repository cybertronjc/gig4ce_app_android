package com.gigforce.lead_management.ui.new_selection_form_submittion_success

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.text.bold
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNewSelectionFormSuccessBinding
import com.gigforce.lead_management.models.WhatsappTemplateModel
import com.gigforce.lead_management.ui.share_application_link.ShareApplicationLinkFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class SelectionFormSubmitSuccessFragment : BaseFragment2<FragmentNewSelectionFormSuccessBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_new_selection_form_success,
    statusBarColor = R.color.lipstick_2
){

    companion object{
        private const val TAG = "SelectionFormSubmitSuccessFragment"
        const val INTENT_EXTRA_WHATSAPP_DATA = "whatsapp_data"
    }

    @Inject
    lateinit var navigation : INavigation
    private lateinit var whatsappTemplateModel: WhatsappTemplateModel

    private val backPressHandler = object : OnBackPressedCallback(true) {

        override fun handleOnBackPressed() {

            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_JOINING,
                false
            )
        }
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }


    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            whatsappTemplateModel = it.getParcelable(INTENT_EXTRA_WHATSAPP_DATA) ?: return@let
        }

        savedInstanceState?.let {
            whatsappTemplateModel = it.getParcelable(INTENT_EXTRA_WHATSAPP_DATA) ?: return@let
        }

        logDataReceivedFromBundles()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INTENT_EXTRA_WHATSAPP_DATA, whatsappTemplateModel)
    }
    private fun logDataReceivedFromBundles() {

        if (::whatsappTemplateModel.isInitialized.not()) {
            logger.e(
                logTag,
                "null whatsappTemplateModel received from bundles",
                Exception("null whatsappTemplateModel received from bundles")
            )
        }
    }

    override fun viewCreated(
        viewBinding: FragmentNewSelectionFormSuccessBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFrom(arguments, savedInstanceState)
        initView()
        initListener()
        addBackPressListener()
    }

    private fun addBackPressListener() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressHandler
        )
    }

    private fun initView() = viewBinding.apply{
        toolbar.setBackButtonDrawable(R.drawable.ic_chevron)
        toolbar.setBackButtonListener {
            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_JOINING,
                false
            )
        }

        shareLinkLayout.setOnClickListener {
            shareToAnyApp(whatsappTemplateModel.shareLink)
        }
    }

    private fun initListener() = viewBinding.apply {

        nextButton.setOnClickListener {
            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_JOINING,
                false
            )
        }
    }

    private fun shareToAnyApp(url: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.app_name)
            )
            //This code needs to be improved. We can't use the resource string value here because this needs dynamic variables.
            val shareMessage = "Congratulations! \nWelcome to ${whatsappTemplateModel.businessName} with Gigforce! I am ${whatsappTemplateModel.tlName} from Gigforce. \uD83D\uDE4F \n\n" +
                    "You are selected as ${whatsappTemplateModel.jobProfileName}. We are delighted to have you on our platform. You are about to start an exciting and rewarding journey with us. \uD83E\uDD1D \n\n" +
                    "With Gigforce - now get transparent rate card and timely payouts. ❤️ \n\n" +
                    "Please complete the joining checklist on Gigforce app. The payouts will be released to the same account that you upload on app. \uD83D\uDC47 \n\n" +
                    "Feel free to reach out to me on ${whatsappTemplateModel.tlMobileNumber} if you have any questions or issues. Happy to assist. \uD83D\uDE0A \n" +
                    " \n" + url

            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
//            val bitmap =
//                BitmapFactory.decodeResource(requireContext().resources, R.drawable.bg_gig_type)
//
//            //save bitmap to app cache folder
//
//            //save bitmap to app cache folder
//            val outputFile = File(requireContext().cacheDir, "share" + ".png")
//            val outPutStream = FileOutputStream(outputFile)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)
//            outPutStream.flush()
//            outPutStream.close()
//            outputFile.setReadable(true, false)
//            shareIntent.putExtra(
//                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
//                    requireContext(),
//                    requireContext().packageName + ".provider",
//                    outputFile
//                )
//            )
            startActivityForResult(
                Intent.createChooser(shareIntent, getString(R.string.choose_one_lead)),
                ShareApplicationLinkFragment.REQUEST_CODE_SHARE_VIA_OTHER_APPS
            )
        } catch (e: Exception) {
            //e.toString();
        }
    }
}