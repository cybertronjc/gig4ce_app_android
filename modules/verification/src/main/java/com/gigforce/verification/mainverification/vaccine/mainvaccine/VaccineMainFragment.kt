package com.gigforce.verification.mainverification.vaccine.mainvaccine

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.remote.verification.VaccineIdLabelReqDM
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
import com.gigforce.core.utils.Lce
import com.gigforce.verification.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.ask_user_for_vaccine_bs.*
import kotlinx.android.synthetic.main.fragment_choose_your_vaccine.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
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
                viewModel.vaccineConfigLiveData.value?.let {
                    when(it){
                        is Lce.Content -> {
                            it.content.let { list ->
                                list[position].vaccineId?.let { vaccineID -> vaccineId = vaccineID }
                                list[position].label?.let { vaccineText ->
                                    vaccineLabel = vaccineText
                                }
                                pickDocument()
                            }
                        }
                        else -> {}
                    }
                }

            }
        }
        viewModel.vaccineConfigLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                }
                is Lce.Content -> {
                    if (it.content.isNullOrEmpty()) {
                        showToast("No vaccine list found!!")
                        navigation.popBackStack()
                    } else {
                        vaccinerv.collection = it.content
                    }
                }
                is Lce.Error -> {
                }
            }
        })

        viewModel.vaccineFileUploadResLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                }
                is Lce.Content -> {
                    viewModel.getVaccineData()
                }
                is Lce.Error -> {
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
                    callKycOcrApi()
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
                        VaccineIdLabelReqDM(vaccineId, vaccineLabel),
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
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cowin.gov.in/"))
                startActivity(browserIntent)
            }
        }
        str.setSpan(clickableSpan, 24, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        download_certificate_tv.movementMethod = LinkMovementMethod.getInstance()
        download_certificate_tv.text = str
    }

}