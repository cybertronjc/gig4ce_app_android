package com.gigforce.verification.mainverification.aadhaarcard

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
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.AppConstants
import com.gigforce.core.StringConstants
import com.gigforce.core.datamodels.verification.AadharCardDataModel
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadhaarCardImageUploadFragmentBinding
import com.gigforce.verification.util.VerificationConstants
import com.google.gson.Gson
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AadhaarCardImageUploadFragment : Fragment(),
    IOnBackPressedOverride {

    companion object {
        fun newInstance() = AadhaarCardImageUploadFragment()
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
    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private val viewModel: AadhaarCardImageUploadViewModel by viewModels()
    private lateinit var viewBinding: AadhaarCardImageUploadFragmentBinding
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
        viewBinding = AadhaarCardImageUploadFragmentBinding.inflate(inflater, container, false)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATON)
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
        val list = listOf(KYCImageModel(text = getString(R.string.upload_pan_card_new), imageIcon = frontUri, imageUploaded = false))
        viewBinding.toplayoutblock.setImageViewPager(list)
        viewBinding.toplayoutblock.setImageViewPager(emptyList())
    }

    private fun initWebview() {
        context?.let {
            viewBinding.digilockerWebview.addJavascriptInterface(WebViewInterface(it), "Android")
            viewBinding.digilockerWebview.settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                loadsImagesAutomatically = true
                domStorageEnabled = true
            }
            viewBinding.digilockerWebview.loadUrl("${iBuildConfig.getPanelBaseUrl()}/kyc/${FirebaseAuthStateListener.getInstance().getCurrentSignInUserInfoOrThrow().uid}")
            viewBinding.digilockerWebview.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                    url?.let {
                        view.loadUrl(it)
                    }
                    return true
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    context?.let {
                        UtilMethods.showLongToast(it, error?.description.toString())
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
            var adharResponseModel = Gson().fromJson(data, AadharResponseModel::class.java)
            if (adharResponseModel.status) {
                verifiedStatusViews()
            } else {
                navigation.popBackStack()
            }
        }
    }

    var allNavigationList = ArrayList<String>()
    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arr ->
                allNavigationList = arr
            }
        } ?: run {
            arguments?.let {
                FROM_CLIENT_ACTIVATON =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
                it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arrData ->
                    allNavigationList = arrData
                }
            }
        }

    }

    private fun listeners() {


        viewBinding.submitButton.setOnClickListener {

            hideSoftKeyboard()
            activity?.onBackPressed()
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
                navigationsForBundle = allNavigationList.slice(IntRange(1, allNavigationList.size - 1)).filter { it.length > 0 }
            }
            navigation.popBackStack()
            navigation.navigateTo(allNavigationList.get(0), bundleOf(VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle))

        }
    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            if (isAadharVerified) {
                var navFragmentsData = activity as NavFragmentsData
                navFragmentsData.setData(
                    bundleOf(
                        StringConstants.BACK_PRESSED.value to true

                    )
                )
            }
            return false
        }
        return false
    }

    var isAadharVerified = false
    private fun observer() {

        viewModel.getVerifiedStatus() //getting userHasVerified status
        viewModel.verifiedStatus.observe(viewLifecycleOwner, Observer {
            it?.let {

                if (it.verified) {
//                    verificationScreenStatus = VerificationScreenStatus.VERIFIED
                    verifiedStatusViews()
                    viewBinding.belowLayout.visible()
                    setAlreadyfilledData(it, false)
                }
                else{
                    viewBinding.belowLayout.gone()
                }
            }
        })
    }

    private fun setAlreadyfilledData(aadharCardDataModel: AadharCardDataModel, enableFields: Boolean) {

        viewBinding.aadharcardTil.editText?.setText(aadharCardDataModel.aadharCardNo?:"")
        viewBinding.nameTilAadhar.editText?.setText(aadharCardDataModel.name?:"")
        aadharCardDataModel.dob?.let {
            if(it.isNotEmpty()){
                viewBinding.dateOfBirthAadhar.text = it
                viewBinding.dobLabel.visible()
            }

        }
        viewBinding.aadharcardTil.editText?.isEnabled = enableFields
        viewBinding.nameTilAadhar.editText?.isEnabled = enableFields
        viewBinding.dateRlAadhar.isEnabled = enableFields
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
        StatusBarUtil.setColorNoTranslucent(requireActivity(), ResourcesCompat.getColor(resources, R.color.lipstick_2, null))
    }

}