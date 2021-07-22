package com.gigforce.ambassador.user_rollment.kycdocs.aadhaarcard

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.ambassador.EnrollmentConstants
import com.gigforce.ambassador.R
import com.gigforce.ambassador.databinding.UserAadhaarCardFragmentBinding
import com.gigforce.ambassador.user_rollment.kycdocs.VerificationConstants
import com.gigforce.ambassador.user_rollment.user_details_filled_dialog.UserDetailsFilledDialogFragment
import com.gigforce.ambassador.user_rollment.user_details_filled_dialog.UserDetailsFilledDialogFragmentResultListener
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.AppConstants
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.gson.Gson
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class UserAadhaarCardFragment : Fragment(), UserDetailsFilledDialogFragmentResultListener {

    companion object {
        fun newInstance() = UserAadhaarCardFragment()
        const val REQUEST_CODE_UPLOAD_AADHAR_IMAGE = 2333

        private const val REQUEST_CAPTURE_IMAGE = 1031
        private const val REQUEST_PICK_IMAGE = 1032

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 103
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var iBuildConfig: IBuildConfig

    private val viewModelUser: UserAadhaarCardViewModel by viewModels()
    private lateinit var viewBinding: UserAadhaarCardFragmentBinding
    private fun activeLoader(activate: Boolean) {
        if (activate) {
            viewBinding.progressBar.visible()
            viewBinding.screenLoaderBar.visible()
            viewBinding.submitButton.isEnabled = false
        } else {
            viewBinding.progressBar.gone()
            viewBinding.screenLoaderBar.gone()
            viewBinding.submitButton.isEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = UserAadhaarCardFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
//        return inflater.inflate(R.layout.aadhaar_card_image_upload_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntent(savedInstanceState)
        initializeImageViews()
        observer()
        listeners()
        initWebview()
    }

    private fun initializeImageViews() {
        viewBinding.toplayoutblock.showUploadHere()
        //ic_pan_illustration
        val frontUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.verification_doc_image))
            .appendPath(resources.getResourceTypeName(R.drawable.verification_doc_image))
            .appendPath(resources.getResourceEntryName(R.drawable.verification_doc_image))
            .build()
        val list = listOf(
            KYCImageModel(
                text = getString(R.string.upload_pan_card_new),
                imageIcon = frontUri,
                imageUploaded = false
            )
        )
        viewBinding.toplayoutblock.setImageViewPager(list)
        viewBinding.toplayoutblock.setImageViewPager(emptyList())
    }

    private fun initWebview() {
        context?.let { fragmentcontext ->
            userId.let {
                viewBinding.digilockerWebview.addJavascriptInterface(
                    WebViewInterface(
                        fragmentcontext
                    ), "Android"
                )
                viewBinding.digilockerWebview.settings.apply {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    loadsImagesAutomatically = true
                    domStorageEnabled = true
                }
                viewBinding.digilockerWebview.loadUrl("${iBuildConfig.getPanelBaseUrl()}/kyc/${it}")
                viewBinding.digilockerWebview.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                        url?.let {
                            view.loadUrl(it)
                        }
                        return true
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        context?.let {
                            UtilMethods.showLongToast(it, error?.description.toString())
                        }
                    }
                }
            }

        }
    }

    inner class WebViewInterface {
        var context: Context
        var data: String? = null

        constructor(context: Context) {
            this.context = context
        }

        @JavascriptInterface
        fun sendData(data: String) {
            this.data = data
            Log.e("javascript", data)
            var adharResponseModel = Gson().fromJson(data, UserAadharResponseModel::class.java)
            if (adharResponseModel.status) {
                verifiedStatusViews()
            } else {
                navigation.popBackStack()
            }
        }
    }

    private lateinit var userId: String
    private lateinit var userName: String
    var allNavigationList = ArrayList<String>()
    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arr ->
                allNavigationList = arr
            }
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: ""
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: ""
        } ?: run {
            arguments?.let {
                it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arrData ->
                    allNavigationList = arrData
                }
                userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: ""
                userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: ""
            }

        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
    }

    private fun listeners() {


        viewBinding.submitButton.setOnClickListener {

            hideSoftKeyboard()
            UserDetailsFilledDialogFragment.launch(
                userId = userId,
                userName = userName,
                fragmentManager = childFragmentManager,
                okayClickListener = this@UserAadhaarCardFragment
            )
//            activity?.onBackPressed()
        }

        viewBinding.appBarAadhar.apply {
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }
    }

    private fun checkForNextDoc() {
        if (allNavigationList.size == 0) {
            activity?.onBackPressed()
        } else {
            var navigationsForBundle = emptyList<String>()
            if (allNavigationList.size > 1) {
                navigationsForBundle =
                    allNavigationList.slice(IntRange(1, allNavigationList.size - 1))
                        .filter { it.length > 0 }
            }
            navigation.popBackStack()
            navigation.navigateTo(
                allNavigationList.get(0), bundleOf(
                    VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle,
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
                )
            )

        }
    }

    private fun observer() {
        userId?.let {
            if(it.isNotBlank())
            viewModelUser.getVerifiedStatus(it) //getting userHasVerified status

        }
        viewModelUser.verifiedStatus.observe(viewLifecycleOwner, Observer {
            it?.let {

                if (it.verified) {
//                    verificationScreenStatus = VerificationScreenStatus.VERIFIED
                    verifiedStatusViews()
                }
            }
        })
    }

    private fun verifiedStatusViews() {
        viewBinding.digilockerWebview.gone()
        viewBinding.toplayoutblock.viewChangeOnVerified()
        viewBinding.belowLayout.gone()
        viewBinding.toplayoutblock.uploadStatusLayout(
            AppConstants.UPLOAD_SUCCESS,
            "VERIFICATION COMPLETED",
            "The Aadhar card details have been verified successfully."
        )
        viewBinding.submitButton.visible()
        viewBinding.submitButton.text = "Next"
        viewBinding.progressBar.gone()
        viewBinding.toplayoutblock.setVerificationSuccessfulView("Aadhaar card verified")


    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
    }

    override fun onOkayClicked() {
        navigation.popBackStack("ambassador/users_enrolled",inclusive = false)
    }

    override fun onReUploadDocumentsClicked() {
        navigation.navigateTo(
            "userinfo/addUserBankDetailsInfoFragment", bundleOf(
                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
            )
        )
    }
}