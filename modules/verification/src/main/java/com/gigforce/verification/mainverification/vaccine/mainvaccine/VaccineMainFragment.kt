package com.gigforce.verification.mainverification.vaccine.mainvaccine

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.common_ui.remote.verification.VaccineIdLabelReqDM
import com.gigforce.core.AppConstants
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.StringConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.verification.R
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.vaccine_main_fragment.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.*
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class VaccineMainFragment : Fragment(), IOnBackPressedOverride {

    private val viewModel: VaccineMainViewModel by viewModels()
    @Inject
    lateinit var navigation : INavigation

    private var userId: String? = null
    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }
    private var userIdToUse: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.vaccine_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData(savedInstanceState)
        observers()
        viewModel.getVaccineData(userIdToUse)
        spannableInit()
        checkIfDocUploadRequire()
        checkIfDocUploadRequireNew()
        listener()
    }

    private fun listener() {
        appBar.setBackButtonListener{
            activity?.onBackPressed()
        }
    }

    private fun checkIfDocUploadRequire() {
        childFragmentManager.setFragmentResultListener(
            "vaccine_doc",
            viewLifecycleOwner
        ) { key, bundle ->
            val result = bundle.getString("vaccine_doc")
            // Do something with the result
            if (result == "try_again") {
                Log.d("droppedInfo", "dropped")
                pickDocument()
            }
        }


    }

    override fun onResume() {
        super.onResume()
        checkIfDocUploadRequireNew()
    }

    private fun requestStoragePermission() {

        if(Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else{

            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    private fun hasStoragePermissions(): Boolean {

        if(Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else{

            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkIfDocUploadRequireNew() {
        val navFragmentsData = activity as NavFragmentsData
        if (navFragmentsData.getData()
                .getString("vaccine_doc") == "try_again"
        ) {
                pickDocument()
                navFragmentsData.setData(bundleOf())
        }
    }

    private var FROM_CLIENT_ACTIVATON: Boolean = false
    var vaccineId = ""
    var vaccineLabel = ""
    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            userId = it.getString(AppConstants.INTENT_EXTRA_UID)
        }?: run {
            arguments?.let {
                FROM_CLIENT_ACTIVATON =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
                userId = it.getString(AppConstants.INTENT_EXTRA_UID)
            }
        }

        userIdToUse = if (userId != null) {
            userId
        }else{
            user?.uid
        }

    }
    private fun observers() {
        vaccinerv.itemClickListener = object : ItemClickListener {
            override fun onItemClick(view: View, position: Int, dataModel: Any) {

                if(position <= -1){
                    if(!hasStoragePermissions()){
                        requestStoragePermission()
                        throw Exception("stroage permission require")
                    }
                    else{
                        if(dataModel is VaccineCertDetailsDM)
                            dataModel.pathOnFirebase?.let { viewModel.downloadFile(it) }
                    }

                }else {
                    viewModel.vaccineConfigLiveData.value?.let {
                        when (it) {
                            is Lce.Content -> {
                                it.content.let { list ->
                                    if (dataModel is VaccineCertDetailsDM) {

                                        dataModel.vaccineId?.let { vaccineID ->
                                            vaccineId = vaccineID
                                        }
                                        dataModel.label?.let { vaccineText ->
                                            vaccineLabel = vaccineText
                                        }
                                        pickDocument()
                                    }
                                }
                            }
                            else -> {
                            }
                        }
                    }
                }

            }
        }
        viewModel.fileDownloaded.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                    progressBar.visible()
                }
                is Lce.Content -> {
                    progressBar.gone()
                    navigation.navigateTo("verification/CertificateDownloadBS")
                }
                is Lce.Error -> {
                    progressBar.gone()
                    Toast.makeText(context, it.error, Toast.LENGTH_LONG).show()
                }
                else -> {

                }
            }
        })
        viewModel.vaccineConfigLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                    progressBar.visible()
                }
                is Lce.Content -> {
                    progressBar.gone()
                    if (it.content.isNullOrEmpty()) {
                        showToast("No vaccine list found!!")
                        navigation.popBackStack()
                    } else {
                        vaccinerv.collection = it.content
                    }
                }
                is Lce.Error -> {
                    progressBar.gone()
                }
            }
        })

        viewModel.vaccineFileUploadResLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                    progressBar.visible()
                }
                is Lce.Content -> {
                    progressBar.gone()
                    navigation.navigateTo("verification/VaccineUploadSuccessfulBS")
                    viewModel.getVaccineData()
                }
                is Lce.Error -> {
                    progressBar.gone()
                }
            }
        })
    }

    var fileUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_DOCUMENT && resultCode == Activity.RESULT_OK) {
            (data?.data ?: return).also {
                fileUri = it
            }
            context?.let { context ->
                fileUri?.let {

                    val fileLength = ImageMetaDataHelpers.getImageLength(context, it)
                    val kb = fileLength/1024
                    val mb = kb/1024
                    if(mb<5) {
                        callKycOcrApi()
                    }
                    else{
                        navigation.navigateTo("verification/SizeWarningBottomSheet")
                    }
                }
            }
        }
    }

    private fun callKycOcrApi() {
        var mutliplartFile: MultipartBody.Part? = null
        fileUri?.let { fileUri ->
                context?.contentResolver?.openInputStream(fileUri)?.let {
                    val byteArr = getBytes(it)
                    val mimeType = MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(context?.contentResolver?.getType(fileUri))
                    val requestFile: RequestBody =
                        RequestBody.create(MediaType.parse(mimeType), byteArr)
                    val pdfname: String =
                        java.lang.String.valueOf(Calendar.getInstance().timeInMillis)
                    mutliplartFile =
                        MultipartBody.Part.createFormData("vaccine", "${pdfname}.pdf", requestFile)
                    mutliplartFile?.let {
                        viewModel.uploadFile(
                            VaccineIdLabelReqDM(vaccineId, vaccineLabel,userIdToUse),
                            it
                        )
                    }
                }

        }

    }

    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream): ByteArray? {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    companion object {
        private const val REQUEST_PICK_DOCUMENT = 202
        private const val REQUEST_STORAGE_PERMISSION = 102
    }

    private fun pickDocument() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        putExtra(
            Intent.EXTRA_MIME_TYPES, arrayOf(
                MimeTypes.PDF
            )
        )
        startActivityForResult(this, REQUEST_PICK_DOCUMENT)
    }

    private fun spannableInit() {
        val str = SpannableString("Don’t have certificate? Click here to download  your certificate and then uplaod.")
        val clickableSpan : ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.cowin.gov.in/")
                )
                startActivity(browserIntent)
            }
        }
        str.setSpan(clickableSpan, 24, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        download_certificate_tv.movementMethod = LinkMovementMethod.getInstance()
        download_certificate_tv.text = str
    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
                val navFragmentsData = activity as NavFragmentsData
                navFragmentsData.setData(
                    bundleOf(
                        StringConstants.BACK_PRESSED.value to true
                    )
                )
        }
        return false
    }

}