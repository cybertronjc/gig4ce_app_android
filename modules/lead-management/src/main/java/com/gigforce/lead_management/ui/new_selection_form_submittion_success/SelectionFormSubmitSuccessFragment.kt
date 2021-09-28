package com.gigforce.lead_management.ui.new_selection_form_submittion_success

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNewSelectionFormSuccessBinding
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
        const val INTENT_EXTRA_SHARE_LINK = "share_link"
    }

    @Inject
    lateinit var navigation : INavigation
    private lateinit var shareLink : String

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            shareLink = it.getString(INTENT_EXTRA_SHARE_LINK) ?: return@let
        }

        savedInstanceState?.let {
            shareLink = it.getString(INTENT_EXTRA_SHARE_LINK) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_SHARE_LINK, shareLink)
    }

    override fun viewCreated(
        viewBinding: FragmentNewSelectionFormSuccessBinding,
        savedInstanceState: Bundle?
    ) {
        initView()
        initListener()
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
            shareToAnyApp(shareLink)
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
                Intent.createChooser(shareIntent, getString(R.string.choose_one_lead)),
                ShareApplicationLinkFragment.REQUEST_CODE_SHARE_VIA_OTHER_APPS
            )
        } catch (e: Exception) {
            //e.toString();
        }
    }
}