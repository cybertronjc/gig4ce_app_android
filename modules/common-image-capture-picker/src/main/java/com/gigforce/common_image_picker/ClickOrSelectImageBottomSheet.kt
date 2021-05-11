package com.gigforce.common_image_picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ClickOrSelectImageBottomSheet : BottomSheetDialogFragment() {

    private var listener: OnPickOrCaptureImageClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_click_or_select_profile_picture, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }


    private fun initView(view : View) {
        view.findViewById<View>(R.id.camera_layout).setOnClickListener {

            listener?.onClickPictureThroughCameraClicked()
            dismiss()
        }
        view.findViewById<View>(R.id.gallery_layout).setOnClickListener {

            listener?.onPickImageThroughCameraClicked()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ClickOrSelectImageBottomSheet"

        fun launch(
            childFragmentManager: FragmentManager,
            listener: OnPickOrCaptureImageClickListener
        ) {

            val fragment = ClickOrSelectImageBottomSheet()
            fragment.listener = listener
            fragment.show(childFragmentManager, TAG)
        }
    }

    interface OnPickOrCaptureImageClickListener {

        fun onClickPictureThroughCameraClicked()

        fun onPickImageThroughCameraClicked()
    }
}