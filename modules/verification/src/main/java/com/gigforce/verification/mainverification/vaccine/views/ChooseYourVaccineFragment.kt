package com.gigforce.verification.mainverification.vaccine.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.remote.verification.VaccineFileUploadReqDM
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.verification.R
import com.gigforce.verification.mainverification.vaccine.VaccineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_choose_your_vaccine.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@AndroidEntryPoint
class ChooseYourVaccineFragment : Fragment() {

    val viewModel: VaccineViewModel by viewModels()

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var gigforceLogger: GigforceLogger
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_choose_your_vaccine, container, false)
    }

    companion object {
        private const val REQUEST_PICK_DOCUMENT = 202
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeners()
        observer()
        spannableInit()
//        navigation.navigateTo("verification/CovidVaccinationCertificateFragment")
    }

    private fun spannableInit() {
        val str = SpannableString("Don’t have certificate? Click here to download  your certificate and then uplaod.")
        val clickableSpan : ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                showToast("link to download certificate")
            }
        }
        str.setSpan(clickableSpan,24,34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        download_certificate_tv.movementMethod = LinkMovementMethod.getInstance()
        download_certificate_tv.text = str
    }

    private fun observer() {

        viewModel.vaccineFileUploadResLiveData.observe(viewLifecycleOwner, Observer {
            if (viewModel.activeObserver) {
                when (it) {
                    Lce.Loading -> {
                        progressBar.visible()
                    }
                    is Lce.Error -> {
                        progressBar.gone()
                        showToast(it.error)
                    }
                    is Lce.Content -> {
                        progressBar.gone()
                        viewModel.activeObserver = false
                        navigation.navigateTo(
                            "verification/CovidVaccinationCertificateFragment",
                            bundleOf("vaccineId" to "vaccine1")
                        )
                    }
                }
            } else {
                viewModel.activeObserver = true
            }
        })
    }

    private fun listeners() {
        confirm_bn.setOnClickListener {
            callKycOcrApi()
        }
        doc_upload_cv.setOnClickListener {
            pickDocument()
        }
        change_vaccine.setOnClickListener{
            navigation.navigateTo("verification/AskUserForVaccineBS")
        }
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

    var fileUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_DOCUMENT && resultCode == Activity.RESULT_OK) {
            (data?.data ?: return).also {
                Log.e("ImageUri", it.toString())
                fileUri = it
            }
            context?.let { context ->
                fileUri?.let {


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
                mutliplartFile =
                    MultipartBody.Part.createFormData("vaccine", "vaccineFile", requestFile)
                mutliplartFile?.let {
                    viewModel.uploadFile(
                        VaccineFileUploadReqDM("vaccine1", "Vaccine1"),
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

}