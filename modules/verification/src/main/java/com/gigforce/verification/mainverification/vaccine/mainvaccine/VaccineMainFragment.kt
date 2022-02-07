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
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.common_ui.remote.verification.VaccineIdLabelReqDM
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.verification.R
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.vaccine_main_fragment.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.*
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class VaccineMainFragment : Fragment() {

    private val viewModel: VaccineMainViewModel by viewModels()
    @Inject
    lateinit var navigation : INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.vaccine_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData()
        observers()
        spannableInit()
        checkIfDocUploadRequire()
        checkIfDocUploadRequireNew()
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

    var vaccineId = ""
    var vaccineLabel = ""
    private fun getIntentData() {
        arguments?.let {
            vaccineId = it.getString("id")?:""
            vaccineLabel = it.getString("label")?:""
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
    val gigforceDirectory: File by lazy {

        File(context?.filesDir, "vaccine").apply {
            if (!this.exists()) {
                mkdirs()
            }
        }
    }

    private val IMAGE_DIRECTORY = "/demonuts_upload_gallery"

    fun getFilePathFromURI(context: Context?, contentUri: Uri?): String? {
        //copy file and send new file path
        val fileName: String? = getFileName(contentUri)
        val wallpaperDirectory: File = File(
            Environment.getExternalStorageDirectory(), IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }
        if (!TextUtils.isEmpty(fileName)) {
            val copyFile = File(wallpaperDirectory.toString() + File.separator + fileName)
            // create folder if not exists
            if (context != null) {
                copy(context, contentUri, copyFile)
            }
            return copyFile.absolutePath
        }
        return null
    }
    fun getFileName(uri: Uri?): String? {
        if (uri == null) return null
        var fileName: String? = null
        val path = uri.path
        val cut = path!!.lastIndexOf('/')
        if (cut != -1) {
            fileName = path.substring(cut + 1)
        }
        return fileName
    }
    fun copy(context: Context, srcUri: Uri?, dstFile: File?) {
        try {
            val inputStream = context.contentResolver.openInputStream(srcUri!!) ?: return
            val outputStream: OutputStream = FileOutputStream(dstFile)
            copystream(inputStream, outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    private val BUFFER_SIZE = 1024 * 2
    @Throws(java.lang.Exception::class, IOException::class)
    fun copystream(input: InputStream?, output: OutputStream?): Int {
        val buffer = ByteArray(BUFFER_SIZE)
        val `in` = BufferedInputStream(input, BUFFER_SIZE)
        val out = BufferedOutputStream(output, BUFFER_SIZE)
        var count = 0
        var n = 0
        try {
            while (`in`.read(buffer, 0, BUFFER_SIZE).also { n = it } != -1) {
                out.write(buffer, 0, n)
                count += n
            }
            out.flush()
        } finally {
            try {
                out.close()
            } catch (e: IOException) {
//                Log.e(e.getMessage(), java.lang.String.valueOf(e))
            }
            try {
                `in`.close()
            } catch (e: IOException) {
//                Log.e(e.getMessage(), java.lang.String.valueOf(e))
            }
        }
        return count
    }

    private fun callKycOcrApi() {
        var mutliplartFile: MultipartBody.Part? = null
        fileUri?.let { fileUri ->
            context?.let {
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
                            VaccineIdLabelReqDM(vaccineId, vaccineLabel),
                            it
                        )
                    }
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

}