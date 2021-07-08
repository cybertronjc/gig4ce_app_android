package com.gigforce.verification.mainverification.pancard

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.datamodels.verification.PanCardDataModel
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.verification.R
import com.gigforce.verification.databinding.PanCardFragmentBinding
import com.gigforce.verification.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.verification.mainverification.Data
import com.gigforce.verification.mainverification.VerificationClickOrSelectImageBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_driving_license.*
import kotlinx.android.synthetic.main.pan_card_fragment.*
import kotlinx.android.synthetic.main.veri_screen_info_component.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.net.URI
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class PanCardFragment() : Fragment(),
    VerificationClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener{

    companion object {
        fun newInstance() = PanCardFragment()
        const val REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333

        const val INTENT_EXTRA_CLICKED_IMAGE_PATH = "clicked_image_path"
        const val INTENT_EXTRA_PAN = "pan"
    }

    @Inject
    lateinit var navigation: INavigation

    private var panCardDataModel: PanCardDataModel? = null
    private var clickedImagePath: Uri? = null
    private val viewModel: PanCardViewModel by viewModels()
    private lateinit var viewBinding: PanCardFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = PanCardFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        observer()
        listeners()
        initViewModel()
    }

    private fun observer() {


    }
    private fun initViewModel() {
        viewModel.kycOcrResult.observe(viewLifecycleOwner, Observer {
            it.let {
                showToast("Ocr status "+  it.status)
            }
        })
        viewModel.kycVerifyResult.observe(viewLifecycleOwner, Observer {
            it.let {
                showToast("Verification "+ it.status)
            }
        })
    }
    private fun listeners() {
        viewBinding.toplayoutblock.setPrimaryClick(View.OnClickListener {
            //call for bottom sheet
            VerificationClickOrSelectImageBottomSheet.launch(parentFragmentManager, "Upload Pan Card", this)
            //launchSelectImageSourceDialog()
        })

        date_rl.setOnClickListener {
            dateOfBirthPicker.show()
        }

        submit_button_pan.setOnClickListener {
            callKycVerificationApi()
        }
    }

    private fun setViews() {
        val frontUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.ic_front))
            .appendPath(resources.getResourceTypeName(R.drawable.ic_front))
            .appendPath(resources.getResourceEntryName(R.drawable.ic_front))
            .build()
        val list = listOf(KYCImageModel(getString(R.string.upload_pan_card_new), frontUri, false))
        viewBinding.toplayoutblock.setImageViewPager(list)
    }

    private fun callKycOcrApi(path: Uri){

        var image: MultipartBody.Part? = null
        if (path != null) {
            val file = File(URI(path.toString()))
            Log.d("Register", "Nombre del archivo " + file.getName())
            // create RequestBody instance from file
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            // MultipartBody.Part is used to send also the actual file name
            image =
                MultipartBody.Part.createFormData("imagenPerfil", file.getName(), requestFile)
        }
        image?.let { viewModel.getKycOcrResult("pan", "", it) }
    }

    private val dateOfBirthPicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                dateOfBirth.setText(DateHelper.getDateInDDMMYYYY(newCal.time))
            },
            1990,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }

    private fun launchSelectImageSourceDialog() {
        val photoCropIntent = Intent()
        photoCropIntent.putExtra("purpose", "verification")
        photoCropIntent.putExtra("fbDir", "/verification/")
        photoCropIntent.putExtra("folder", "verification")
        photoCropIntent.putExtra("detectFace", 0)
        photoCropIntent.putExtra("file", "pan_card.jpg")
        navigation.navigateToPhotoCrop(photoCropIntent,
            AddPanCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE, requireContext(),this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPanCardInfoFragment.REQUEST_CODE_UPLOAD_PAN_IMAGE) {

            if (resultCode == Activity.RESULT_OK) {
                clickedImagePath =
                    data?.getParcelableExtra("uri")
                showPanInfoCard(clickedImagePath!!)

//                if (panDataCorrectCB.isChecked)
//                    enableSubmitButton()
//
//                if (clickedImagePath != null && panSubmitSliderBtn.isGone) {
//                    panSubmitSliderBtn.visible()
//                    panDataCorrectCB.visible()
//                }

            }
        }
    }

//    private fun showImageInfoLayout() {
//        panInfoLayout.visibility = View.VISIBLE
//    }
//
//    private fun showPanImageLayout() {
//        panImageHolder.visibility = View.VISIBLE
//    }
//
//    private fun hidePanImageAndInfoLayout() {
//        panImageHolder.visibility = View.GONE
//        panInfoLayout.visibility = View.GONE
//    }
//
//    private fun enableSubmitButton() {
//        panSubmitSliderBtn.isEnabled = true
//
//        panSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_pink, null)
//        panSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.lipstick, null)
//    }
//
//    private fun disableSubmitButton() {
//        panSubmitSliderBtn.isEnabled = false
//
//        panSubmitSliderBtn.outerColor =
//            ResourcesCompat.getColor(resources, R.color.light_grey, null)
//        panSubmitSliderBtn.innerColor =
//            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
//    }

//    override fun onImageSourceSelected(source: ImageSource) {
//        showImageInfoLayout()
//
//        if (panDataCorrectCB.isChecked)
//            enableSubmitButton()
//    }
    private fun callKycVerificationApi(){
        var list = listOf(Data("name", name_til.editText?.text.toString()),
            Data("no", pan_til.editText?.text.toString()),
            Data("dob", dateOfBirth.text.toString()))
        viewModel.getKycVerificationResult("pan", list)
    }

    private fun showPanInfoCard(panInfoPath: Uri) {
        viewBinding.toplayoutblock.setDocumentImage(0, panInfoPath)
        //call ocr api
        callKycOcrApi(panInfoPath)
    }
    override fun onClickPictureThroughCameraClicked() {
        launchSelectImageSourceDialog()
    }

    override fun onPickImageThroughCameraClicked() {
        launchSelectImageSourceDialog()
    }
}