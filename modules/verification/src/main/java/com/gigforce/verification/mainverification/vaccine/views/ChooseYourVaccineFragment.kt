package com.gigforce.verification.mainverification.vaccine.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.remote.verification.VaccineFileUploadReqDM
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lce
import com.gigforce.verification.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_choose_your_vaccine.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.net.URI
import javax.inject.Inject

@AndroidEntryPoint
class ChooseYourVaccineFragment : Fragment() {

    val viewModel : VaccineViewModel by viewModels()
    @Inject
    lateinit var navigation : INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_choose_your_vaccine, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeners()
        observer()
//        navigation.navigateTo("verification/CovidVaccinationCertificateFragment")
    }

    private fun observer() {
        viewModel.vaccineFileUploadResLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                Lce.Loading -> {}
                is Lce.Error -> {}
                is Lce.Content -> {
                    navigation.navigateTo("verification/CovidVaccinationCertificateFragment")
                }

            }
        })
    }

    private fun listeners() {
        doc_upload_cv.setOnClickListener{
            callKycOcrApi()
        }
    }

    var imageUriResultCrop: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImageCropActivity.CROP_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            imageUriResultCrop =
                Uri.parse(data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA))
            context?.let {
                GlideApp.with(it)
                    .load(imageUriResultCrop)
                    .into(doc_iv)
            }
        }
    }


    private fun callKycOcrApi(){

        var image: MultipartBody.Part? = null
        if (imageUriResultCrop != null) {
            val file = File(URI(imageUriResultCrop.toString()))
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("image/png"), file)
            image =
                MultipartBody.Part.createFormData("vaccine", file.name, requestFile)
        }
        image?.let {
            viewModel.uploadFile(
                VaccineFileUploadReqDM("vaccine1","Vaccine1"),
                it
            )
        }
    }
}