package com.gigforce.client_activation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.gigforce.core.StringConstants
import com.gigforce.client_activation.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.client_activation_click_or_select_image.*


class ClientActivationClickOrSelectImageBottomSheet : BottomSheetDialogFragment() {

    private var sheetTitle: String? = ""
    private var listener: OnPickOrCaptureImageClickListener? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.PHOTO_PIC_TITLE.value, resources.getString(R.string.upload_photo_client))
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            sheetTitle =
                it.getString(StringConstants.PHOTO_PIC_TITLE.value, resources.getString(R.string.upload_photo_client))

        }

        arguments?.let {
            sheetTitle =
                it.getString(StringConstants.PHOTO_PIC_TITLE.value, resources.getString(R.string.upload_photo_client))

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.client_activation_click_or_select_image, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initView()
    }


    private fun initView() {
        change_photo_label.text= sheetTitle
        camera_layout.setOnClickListener {
            listener?.onClickPictureThroughCameraClicked()
            dismiss()
        }
        gallery_layout.setOnClickListener {
            listener?.onPickImageThroughCameraClicked()
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

            val fragment = ClientActivationClickOrSelectImageBottomSheet()
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