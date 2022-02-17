package com.gigforce.verification.mainverification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.gigforce.core.StringConstants
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.verification.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.verification_fragment_click_or_select_image.*
import javax.inject.Inject

@AndroidEntryPoint
class VerificationClickOrSelectImageBottomSheet : BottomSheetDialogFragment() {

    private var sheetTitle: String? = ""
    private var listener: OnPickOrCaptureImageClickListener? = null
    @Inject
    lateinit var gigforceLogger: GigforceLogger
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.PHOTO_PIC_TITLE.value, resources.getString(R.string.upload_photo_veri))
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            sheetTitle =
                it.getString(StringConstants.PHOTO_PIC_TITLE.value, getString(R.string.upload_photo_veri))

        }

        arguments?.let {
            sheetTitle =
                it.getString(StringConstants.PHOTO_PIC_TITLE.value, getString(R.string.upload_photo_veri))

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.verification_fragment_click_or_select_image, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initView()
    }


    private fun initView() {
        change_photo_label.text= sheetTitle?:resources.getString(R.string.upload_photo_veri)
        camera_layout.setOnClickListener {
            listener?.onClickPictureThroughCameraClicked()
            dismiss()
        }
        gallery_layout.setOnClickListener {
            try {
                listener?.onPickImageThroughCameraClicked()
            }catch (e:Exception){
                gigforceLogger.d("Gallery imagepicker exception",e.toString())
            }
            dismiss()
        }

    }

    companion object {
        const val TAG = "ClickOrSelectImageBottomSheet"

        fun launch(
            childFragmentManager: FragmentManager,
            sheetTitle: String,
            listener: OnPickOrCaptureImageClickListener
        ) {

            val fragment = VerificationClickOrSelectImageBottomSheet()
            fragment.arguments =
                bundleOf(StringConstants.PHOTO_PIC_TITLE.value to sheetTitle)
            fragment.listener = listener
            fragment.show(childFragmentManager, TAG)
        }
    }

    interface OnPickOrCaptureImageClickListener {

        fun onClickPictureThroughCameraClicked()

        fun onPickImageThroughCameraClicked()

    }
}