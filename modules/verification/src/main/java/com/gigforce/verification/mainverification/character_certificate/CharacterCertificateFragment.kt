package com.gigforce.verification.mainverification.character_certificate

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.StringConstants
import com.gigforce.core.extensions.getDownloadUrlOrThrow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.verification.databinding.FragmentCharacterCertificateBinding
import com.gigforce.verification.util.VerificationConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class CharacterCertificateFragment : Fragment(), IOnBackPressedOverride {

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    private lateinit var viewBinding: FragmentCharacterCertificateBinding
    private var userId: String? = null
    private val user: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }
    private var userIdToUse: String? = null

    private val viewModel : CharacterCertificateViewModel by viewModels()

    private var FROM_CLIENT_ACTIVATON: Boolean = false

    var currentFilePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentCharacterCertificateBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getDataFromIntent(savedInstanceState)
        initviews()
        listeners()
        observer()
    }

    var allNavigationList = ArrayList<String>()
    var intentBundle: Bundle? = null
    private fun getDataFromIntent(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
            userId = it.getString(AppConstants.INTENT_EXTRA_UID) ?: return@let
        } ?: run {
            arguments?.let {
                FROM_CLIENT_ACTIVATON =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
                it.getStringArrayList(VerificationConstants.NAVIGATION_STRINGS)?.let { arrData ->
                    allNavigationList = arrData
                }
                intentBundle = it
                userId = it.getString(AppConstants.INTENT_EXTRA_UID) ?: return@let
            }
        }

    }

    private fun initviews() {
        userIdToUse = if (userId != null) {
            userId
        }else{
            user?.uid
        }
    }

    private fun listeners() = viewBinding.apply{

        appBarCharacter.apply {
            makeBackgroundMoreRound()
            changeBackButtonDrawable()
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }
        uploadButton.setOnClickListener {
            //navigate to select document
            if (!isStoragePermissionGranted()) {
                askForStoragePermission()
            } else {
               pickDocument()
            }
        }

        okayButton.setOnClickListener {
            checkForNextDoc()
        }

        editIcon.setOnClickListener {
            if (!isStoragePermissionGranted()) {
                askForStoragePermission()
            } else {
                pickDocument()
            }
        }

        downloadIcon.setOnClickListener {
            currentFilePath?.let {
                it1 -> lifecycleScope.launch {
                startDocumentDownload(it1) }
            }
        }

    }

    private fun pickDocument() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/pdf"
        putExtra(
            Intent.EXTRA_MIME_TYPES, arrayOf(
                MimeTypes.PDF
            )
        )
        startActivityForResult(this, REQUEST_PICK_DOCUMENT)
    }

    private fun observer() = viewBinding.apply{

        viewModel.getCharacterData()
        viewModel.characterLiveData.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it) {
                Lce.Loading -> {
                    progressBarC.visible()
                }
                is Lce.Content -> {
                    progressBarC.gone()
                    if (it.content.status && it.content.data != null){
                        uploadButton.gone()
                        editIcon.visible()
                        downloadIcon.visible()
                        it.content.data?.path.let {
                            currentFilePath = it
                        }
                    }

                }
                is Lce.Error -> {
                    progressBarC.gone()
                    uploadButton.visible()
                    editIcon.gone()
                    downloadIcon.gone()
                }
            }
        })

        viewModel.fileDownloaded.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                    progressBarC.visible()
                }
                is Lce.Content -> {
                    progressBarC.gone()
                    navigation.navigateTo("verification/CertificateDownloadBS")
                }
                is Lce.Error -> {
                    progressBarC.gone()
                    Toast.makeText(context, it.error, Toast.LENGTH_LONG).show()
                }
                else -> {

                }
            }
        })

        viewModel.characterFileUploadResLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                    progressBarC.visible()
                }
                is Lce.Content -> {
                    progressBarC.gone()
                    if (it.content.status == true){
                        navigation.navigateTo("verification/VaccineUploadSuccessfulBS")
                        viewModel.getCharacterData()
                    } else {
                        showToast("Certificate upload failed, try again")
                    }

                }
                is Lce.Error -> {
                    progressBarC.gone()
                }
            }
        })
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

            intentBundle?.putStringArrayList(
                com.gigforce.common_ui.StringConstants.NAVIGATION_STRING_ARRAY.value,
                java.util.ArrayList(navigationsForBundle)
            )
            navigation.navigateTo(
                allNavigationList.get(0), intentBundle
            )
        }
    }

    private fun askForStoragePermission() {
        Log.v("CharacterCertificateFragment", "Permission Required. Requesting Permission")
        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

        } else {

            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    private fun isStoragePermissionGranted(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        } else {
            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
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
                      callDocumentUploadAPI()

                }
            }
        }
    }

    private fun callDocumentUploadAPI() {
        var mutliplartFile: MultipartBody.Part? = null
        fileUri?.let { fileUri ->
            context?.contentResolver?.openInputStream(fileUri)?.let {
                val byteArr = getBytes(it)
                val mimeType = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(context?.contentResolver?.getType(fileUri))
                if (mimeType.toString().contains("pdf")) {
                    val requestFile: RequestBody =
                        RequestBody.create(MediaType.parse(mimeType), byteArr)
                    val pdfname: String =
                        java.lang.String.valueOf(Calendar.getInstance().timeInMillis)
                    val reqBodyUpdatedBy =
                        RequestBody.create(MediaType.parse("text/plain"), FirebaseAuthStateListener.getInstance().getCurrentSignInInfo()?.uid.toString())
                    val reqBodyUpdatedAt =
                        RequestBody.create(MediaType.parse("text/plain"), Date().toString())
                    mutliplartFile =
                        MultipartBody.Part.createFormData(
                            "file",
                            "${pdfname}.pdf",
                            requestFile
                        )
                    mutliplartFile?.let {
                        viewModel.uploadCharacterCertificate(
                            it,
                            reqBodyUpdatedBy,
                            reqBodyUpdatedAt
                        )
                    }
                } else {
                    navigation.navigateTo("verification/InvalidFormatBottomSheet")
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
        fun newInstance() = CharacterCertificateFragment()

        private const val REQUEST_STORAGE_PERMISSION = 104
        private const val REQUEST_PICK_DOCUMENT = 202
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

    private suspend fun startDocumentDownload(
        url: String
    ) {
        try {
            val filePathName = FirebaseUtils.extractFilePath(url)
            val fullDownloadLink = FirebaseStorage.getInstance().reference.child(url).getDownloadUrlOrThrow().toString()
            val downloadRequest = DownloadManager.Request(Uri.parse(fullDownloadLink)).run {
                setTitle(filePathName)
                setDescription("Character Certificate")
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    filePathName
                )
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
            }

            val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(downloadRequest)
            showToast("Saving file in Downloads, check notification...")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}